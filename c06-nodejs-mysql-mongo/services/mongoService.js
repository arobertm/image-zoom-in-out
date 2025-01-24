// services/mongoService.js
const mongoose = require('mongoose');
const snmp = require('net-snmp');

class MongoService {
    constructor() {
        this.MetricsSchema = new mongoose.Schema({
            serverId: {
                type: String,
                required: true
            },
            osName: {
                type: String,
                required: true
            },
            cpuUsage: {
                type: Number,
                required: true
            },
            ramUsage: {
                type: Number,
                required: true
            }
        }, {
            timestamps: true
        });

        this.Metrics = mongoose.model('Metrics', this.MetricsSchema);
        this.snmpSession = null;
    }

    async initialize() {
        try {
            await mongoose.connect('mongodb://localhost:27017/metrics');
            console.log('Connected to MongoDB');

            await this.ensureCollection();
            
            this.setupSNMP();
            
            console.log('MongoDB service initialized completely');
        } catch (err) {
            console.error('MongoDB initialization error:', err);
            throw err;
        }
    }

    setupSNMP() {
        try {
            const callback = (error, trap) => {
                if (error) {
                    console.error('SNMP Error:', error);
                } else {
                    this.handleSNMPTrap(trap);
                }
            };

            const options = {
                port: 161,
                disableAuthorization: true,
                accessControlModelType: snmp.AccessControlModelType.None,
                engineID: "8000B98380XXXXXXXXXXXXXXXXXXXXXXXX",
                address: "0.0.0.0",
                transport: "udp4"
            };

            this.snmpSession = snmp.createReceiver(options, callback);
            console.log('SNMP Trap receiver started on port 161');
        } catch (error) {
            console.error('Error setting up SNMP:', error);
        }
    }

    async handleSNMPTrap(trap) {
        try {
            console.log('Received SNMP trap:', trap);
            
            const metrics = {
                serverId: this.extractValue(trap, 0),
                osName: this.extractValue(trap, 1),
                cpuUsage: parseFloat(this.extractValue(trap, 2)) || 0,
                ramUsage: parseFloat(this.extractValue(trap, 3)) || 0
            };

            console.log('Parsed metrics:', metrics);

            await this.saveMetrics(metrics);
            console.log('Metrics saved to MongoDB successfully');
        } catch (error) {
            console.error('Error handling SNMP trap:', error);
        }
    }

    extractValue(trap, index) {
        try {
            return trap.pdu.varbinds[index]?.value?.toString() || 'unknown';
        } catch (error) {
            console.error(`Error extracting value at index ${index}:`, error);
            return 'unknown';
        }
    }

    async ensureCollection() {
        try {
            const collections = await mongoose.connection.db.listCollections().toArray();
            const metricsExists = collections.some(col => col.name === 'metrics');

            if (!metricsExists) {
                await this.Metrics.create({
                    serverId: 'RMIServer1',
                    osName: 'Initial Setup',
                    cpuUsage: 0,
                    ramUsage: 0
                });
                console.log('Metrics collection created successfully');
            }
        } catch (error) {
            console.error('Error ensuring collection:', error);
            throw error;
        }
    }

    async saveMetrics(metrics) {
        try {
            const newMetrics = new this.Metrics(metrics);
            const saved = await newMetrics.save();
            console.log('Metrics saved:', saved);
            return saved;
        } catch (error) {
            console.error('Error saving metrics:', error);
            throw error;
        }
    }

    async getMetrics(serverId) {
        try {
            const query = serverId ? { serverId } : {};
            return await this.Metrics.find(query)
                .sort({ createdAt: -1 })
                .limit(50);
        } catch (error) {
            console.error('Error getting metrics:', error);
            throw error;
        }
    }

    async getLatestMetrics() {
        try {
            return await this.Metrics.find()
                .sort({ createdAt: -1 })
                .limit(10);
        } catch (error) {
            console.error('Error getting latest metrics:', error);
            throw error;
        }
    }

    async cleanup() {
        try {
            if (this.snmpSession) {
                this.snmpSession.close();
                console.log('SNMP session closed');
            }
            await mongoose.connection.close();
            console.log('MongoDB connection closed');
        } catch (error) {
            console.error('Error during cleanup:', error);
        }
    }

    isConnected() {
        return mongoose.connection.readyState === 1;
    }
}

module.exports = new MongoService();