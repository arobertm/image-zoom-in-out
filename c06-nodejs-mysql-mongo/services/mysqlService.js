const { Sequelize, DataTypes } = require('sequelize');

class MySQLService {
    constructor() {
        this.sequelize = new Sequelize('imagedb', 'root', 'root', {
            host: 'localhost',
            dialect: 'mysql',
            dialectOptions: {
                maxAllowedPacket: 1073741824 
            },
            pool: {
                max: 5,
                min: 0,
                acquire: 60000,
                idle: 10000
            }
        });

        this.Image = this.sequelize.define('processed_image', {
            imageId: {
                type: DataTypes.STRING,
                allowNull: false,
                unique: true
            },
            zoomLevel: {
                type: DataTypes.DOUBLE,
                allowNull: false
            },
            imageData: {
                type: DataTypes.BLOB('long'),
                allowNull: false
            }
        }, {
            charset: 'binary'
        });
    }

    async initialize() {
        try {
            await this.sequelize.authenticate();
            console.log('Connection to MySQL has been established successfully.');
            await this.sequelize.sync();
            console.log('MySQL tables created!');
        } catch (err) {
            console.error('Unable to connect to MySQL:', err);
            throw err;
        }
    }

    async saveImage(imageId, zoomLevel, imageData) {
        return await this.Image.create({
            imageId,
            zoomLevel,
            imageData
        });
    }

    async getImage(imageId) {
        return await this.Image.findOne({
            where: { imageId }
        });
    }

    async cleanup() {
        await this.sequelize.close();
    }
}

module.exports = new MySQLService();