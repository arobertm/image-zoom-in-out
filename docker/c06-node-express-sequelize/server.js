const express = require('express');
const { Sequelize, DataTypes } = require('sequelize');
const app = express();

app.use(express.raw({
    type: 'application/octet-stream',
    limit: 'Infinity'
}));

const sequelize = new Sequelize('imagedb', 'root', 'root', {
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

sequelize.authenticate()
    .then(() => {
        console.log('Connection to MySQL has been established successfully.');
    })
    .catch(err => {
        console.error('Unable to connect to the database:', err);
    });

// Definim modelul cu BLOB pentru date binare
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

sequelize.sync()
    .then(() => {
        console.log('Database & tables created!');
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

        // Logging pentru debugging
        console.log("Retrieved image data length:", image.imageData.length);
        console.log("First 4 bytes:", Array.from(image.imageData.slice(0, 4)).map(b => b.toString(16)));
        
        res.setHeader('Content-Type', 'image/bmp');
        res.setHeader('Content-Transfer-Encoding', 'binary');
        res.setHeader('Content-Length', image.imageData.length);
        
        res.send(image.imageData);
    } catch (error) {
        console.error('Error retrieving image:', error);
        res.status(500).json({ message: "Error retrieving image", error: error.message });
    }
});

app.get('/', (req, res) => {
    res.json({ message: 'Image processing server is running' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});