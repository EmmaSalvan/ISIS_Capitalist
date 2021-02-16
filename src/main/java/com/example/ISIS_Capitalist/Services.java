/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.ISIS_Capitalist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author emmasalvan
 */
public class Services {

    World world = new World();
    
    World readWorldFromXml() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            world = (World) u.unmarshal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return world;
    }

    void saveWorldToXml(World world) {
        try {
            OutputStream output = new FileOutputStream("world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, output);
        } catch (Exception ex) {
            Logger.getLogger(Services.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    World getWorld() {
        return readWorldFromXml();
    }
}
