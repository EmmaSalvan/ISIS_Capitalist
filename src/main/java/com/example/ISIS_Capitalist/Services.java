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
import java.util.List;
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
   

    World readWorldFromXml(String username) throws JAXBException {
        try {
            File file = new File(username + "-world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            world = (World) u.unmarshal(file);
        } catch (Exception e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            world = (World) u.unmarshal(input);
        }
        return world;
    }

    void saveWorldToXml(String username, World world) {
        try {
            OutputStream output = new FileOutputStream(username + "-world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, output);
        } catch (Exception ex) {
            Logger.getLogger(Services.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    World getWorld(String username) throws JAXBException {
        return readWorldFromXml(username);
    }
    
    void updateWorld(World world){
        long diff = System.currentTimeMillis() - world.getLastupdate();
        
        List<ProductType> produits = (List<ProductType>) world.getProducts();
        for (ProductType p : produits) {
            // Le produit n'a pas de manager
            if (!p.isManagerUnlocked()){
                // Le produit a été créé
                if(p.getTimeleft()!=0 && p.getTimeleft()<diff){
                    double newScore = world.getScore() + p.getRevenu();
                    world.setScore(newScore);
                    double newMoney = world.getMoney() + p.getRevenu();
                    world.setMoney(newMoney);
                }
                // Le produit n'a pas été créé
                else {
                    long newTimeLeft = p.getTimeleft() - diff;
                    p.setTimeleft(newTimeLeft);
                }
            }
            else {
                long vitesse = p.getVitesse();
                long nbProd = (int) diff / vitesse;
                // On met à jour le score et l'argent du monde en fonction du nombre de produit créé
                double newScore = world.getScore() + (p.getRevenu()*nbProd);
                world.setScore(newScore);
                double newMoney = world.getMoney() + (p.getRevenu()*nbProd);
                world.setMoney(newMoney);
                                
                //On calcule le temps restant 
                long timeRestant = vitesse - diff % vitesse;
                p.setTimeleft(timeRestant);
            }
        }
        world.setLastupdate(System.currentTimeMillis());
    }

    public ProductType findProductById(World world, int id) {
        ProductType prod = null;
        for (ProductType p : world.getProducts().product) {
            if (id == p.id) {
                prod = p;
            }
        }
        return prod;
    }

    // prend en paramètre le pseudo du joueur et le produit
    // sur lequel une action a eu lieu (lancement manuel de production ou achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        world.setLastupdate(System.currentTimeMillis());
        // trouver dans ce monde, le produit équivalent à celui passé en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            double money = world.getMoney();
            double crois = product.getCroissance();
            double cout = product.getCout();
            int qt = product.getQuantite();
            int newQt = newproduct.getQuantite();
            // soustraire de l'argent du joueur le cout de la quantité achetée
            //double newCout = cout * ((1 - (Math.pow(crois, qtchange))) / (1 - crois));
            double newCout = Math.round(cout * Math.pow(crois, qtchange) *100)/100;
            double newMoney = money - newCout;
            // et mettre à jour la quantité de product
            world.setMoney(newMoney);
            product.setQuantite(newQt);
            // mettre à jour le cout
            product.setCout(newCout);
            // mettre à jour le revenu
            if (qt != 0) {
                double newRevenu = (product.getRevenu() / qt) * newQt;
                product.setRevenu(newRevenu);
            }

        } else {
            // initialiser product.timeleft à product.vitesse pour lancer la production
            product.timeleft = product.vitesse;
        }
        // sauvegarder les changements du monde
        saveWorldToXml(username, world);
        return true;
    }

    public PallierType findManagerByName(World world, String name) {
        PallierType mana = null;
        for (PallierType p : world.getManagers().pallier) {
            if (name.equals(p.getName())) {
                mana = p;
            }
        }
        return mana;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }
        // débloquer ce manager
        manager.setUnlocked(true);

        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        double money = world.getMoney();
        double seuil = manager.getSeuil();
        
        double newMoney = money - seuil;
        world.setMoney(newMoney);
        
        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }
}
