package com.ejb;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ImageRMIInterface extends Remote {
    byte[] zoomImage(byte[] imageData, double zoomLevel, String imageId) throws RemoteException;
}