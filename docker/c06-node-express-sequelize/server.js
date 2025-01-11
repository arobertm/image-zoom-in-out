const express = require('express');
const { Sequelize, DataTypes } = require('sequelize');
const app = express();

// Middleware
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true }));


const sequelize = new Sequelize('imagedb', 'root', 'root', {
    host: 'localhost',
    dialect: 'mysql'
});

sequelize.authenticate()
    .then(() => {
        console.log('Connection to MySQL has been established successfully.');
    })
    .catch(err => {
        console.error('Unable to connect to the database:', err);
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
});


sequelize.sync()
    .then(() => {
        console.log('Database & tables created!');
    });

app.post('/api/images', async (req, res) => {
    try {
        const { imageId, zoomLevel, imageData } = req.body;
        
        if (!imageId || !imageData || !zoomLevel) {
            return res.status(400).json({ message: "Missing required fields" });
        }

        const imageBuffer = Buffer.from(imageData, 'base64');
        
        const savedImage = await Image.create({
            imageId: imageId,
            zoomLevel: zoomLevel,
            imageData: imageBuffer
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