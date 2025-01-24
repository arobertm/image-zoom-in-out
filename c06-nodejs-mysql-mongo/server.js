const express = require('express');
const { Sequelize, DataTypes } = require('sequelize');
const mongoService = require('./services/mongoService');
const app = express();

app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, image-id, zoom-level');
    
    if (req.method === 'OPTIONS') {
        return res.status(200).json({
            status: 'ok'
        });
    }
    
    next();
});

app.use(express.raw({
    type: 'application/octet-stream',
    limit: 'Infinity'
}));
app.use(express.json());

const sequelize = new Sequelize('imagedb', 'root', 'root', {
    host: 'localhost',
    dialect: 'mysql',
    dialectOptions: {
        connectTimeout: 60000,
    },
    pool: {
        max: 5,
        min: 0,
        acquire: 60000,
        idle: 10000
    }
}); 

const Image = sequelize.define('processed_image', {
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

app.post('/api/images', async (req, res) => {
    try {
        const imageId = req.headers['image-id'];
        const zoomLevel = parseFloat(req.headers['zoom-level']);
        
        if (!imageId || !req.body || !zoomLevel) {
            return res.status(400).json({ message: "Missing required fields" });
        }

        const savedImage = await Image.create({
            imageId: imageId,
            zoomLevel: zoomLevel,
            imageData: req.body 
        });

        res.json({
            message: "Image stored successfully",
            downloadUrl: `/api/images/${imageId}`
        });
    } catch (error) {
        console.error('Error storing image:', error);
        res.status(500).json({ message: "Error storing image", error: error.message });
    }
});

app.get('/api/images/:imageId', async (req, res) => {
    try {
        const { imageId } = req.params;
        const image = await Image.findOne({
            where: { imageId: imageId }
        });

        if (!image) {
            return res.status(404).json({ message: "Image not found" });
        }
        
        res.setHeader('Content-Type', 'image/bmp');
        res.setHeader('Content-Transfer-Encoding', 'binary');
        res.setHeader('Content-Length', image.imageData.length);
        res.send(image.imageData);
    } catch (error) {
        console.error('Error retrieving image:', error);
        res.status(500).json({ message: "Error retrieving image", error: error.message });
    }
});

app.get('/api/metrics', async (req, res) => {
    try {
        const metrics = await mongoService.getLatestMetrics();
        res.json(metrics);
    } catch (error) {
        console.error('Error fetching metrics:', error);
        res.status(500).json({ message: "Error fetching metrics", error: error.message });
    }
});

app.get('/api/metrics/:serverId', async (req, res) => {
    try {
        const metrics = await mongoService.getMetrics(req.params.serverId);
        res.json(metrics);
    } catch (error) {
        console.error('Error fetching server metrics:', error);
        res.status(500).json({ message: "Error fetching metrics", error: error.message });
    }
});

app.get('/health', async (req, res) => {
    res.json({
        mysql: await sequelize.authenticate().then(() => true).catch(() => false),
        mongodb: mongoService.isConnected()
    });
});

async function startServer() {
    try {

        await sequelize.authenticate();
        console.log('MySQL connection established');
        await sequelize.sync();
        console.log('MySQL tables synchronized');

   
        await mongoService.initialize();
        console.log('MongoDB initialized');

        const PORT = process.env.PORT || 3000;
        app.listen(PORT, () => {
            console.log(`Server is running on port ${PORT}`);
        });
    } catch (error) {
        console.error('Failed to start server:', error);
        process.exit(1);
    }
}

process.on('SIGTERM', async () => {
    console.log('SIGTERM received. Shutting down...');
    await sequelize.close();
    await mongoService.cleanup();
    process.exit(0);
});

startServer();