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

    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
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
        World world1 = readWorldFromXml(username);
        //updateWorld(world1, username);
        saveWorldToXml(username, world1);
        return world1;
    }

    public void deleteWorld(String username) throws JAXBException {
        World wrd = readWorldFromXml(username);
        double activeangels = wrd.getActiveangels();
        double totalangels = wrd.getTotalangels();
        double angels = nombreAnges(world);
        activeangels += activeangels + angels;
        totalangels += totalangels + angels;
        double score = wrd.getScore();

        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        world = (World) u.unmarshal(input);
        world.setActiveangels(activeangels);
        world.setTotalangels(totalangels);
        world.setScore(score);
        saveWorldToXml(username, world);
        
    }

    void updateWorld(World world, String username) {
        long diff = System.currentTimeMillis() - world.getLastupdate();
        int angeBonus = world.getAngelbonus();
        List<ProductType> produits = (List<ProductType>) world.getProducts();
        for (ProductType p : produits) {
            // Le produit n'a pas de manager
            if (!p.isManagerUnlocked()) {
                // Le produit a été créé
                if (p.getTimeleft() != 0 && p.getTimeleft() < diff) {
                    double newScore = world.getScore() + p.getRevenu() * (1 + world.getActiveangels() * angeBonus / 100);
                    world.setScore(newScore);
                    double newMoney = world.getMoney() + p.getRevenu() * (1 + world.getActiveangels() * angeBonus / 100);
                    world.setMoney(newMoney);
                } // Le produit n'a pas été créé
                else {
                    long newTimeLeft = p.getTimeleft() - diff;
                    p.setTimeleft(newTimeLeft);
                }
            } else {
                long vitesse = p.getVitesse();
                long nbProd = (int) diff / vitesse;
                // On met à jour le score et l'argent du monde en fonction du nombre de produit créé
                double newScore = world.getScore() + (p.getRevenu() * nbProd * (1 + world.getActiveangels() * angeBonus / 100));
                world.setScore(newScore);
                double newMoney = world.getMoney() + (p.getRevenu() * nbProd * (1 + world.getActiveangels() * angeBonus / 100));
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
            double newCout = cout * Math.pow(crois, qtchange);
            double newMoney = money - cout;
            world.setMoney(newMoney);
            // et mettre à jour la quantité de product
            product.setQuantite(newQt);
            // mettre à jour le cout
            product.setCout(newCout);
            System.out.println(world.getMoney());

        } else {
            // initialiser product.timeleft à product.vitesse pour lancer la production
            product.timeleft=0;
            System.out.println(product.getTimeleft());
            world.setMoney(world.getMoney() + (product.getRevenu() * product.getQuantite()));
        }

        List<PallierType> unlocks = (List<PallierType>) product.getPalliers().getPallier();
        for (PallierType u : unlocks) {
            if (u.isUnlocked() == false && product.getQuantite() >= u.getSeuil()) {
                calcUpgrade(u, product);
            }
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

    public PallierType findUpgradeByName(World world, String name) {
        PallierType up = null;
        for (PallierType p : world.getUpgrades().pallier) {
            if (name.equals(p.getName())) {
                up = p;
            }
        }
        return up;
    }

    public Boolean updateUpgrade(String username, PallierType newupgrade) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, l'upgrade équivalent à celui passé en paramètre
        PallierType upgrade = findUpgradeByName(world, newupgrade.getName());
        if (upgrade == null) {
            return false;
        }
        // débloquer ce cash upgrade
        upgrade.setUnlocked(true);
        // trouver le produit correspondant a l'upgrade
        ProductType product = findProductById(world, upgrade.getIdcible());
        if (product == null) {
            return false;
        }
        // soustraire de l'argent du joueur le cout du cash upgrade
        double money = world.getMoney();
        double seuil = upgrade.getSeuil();

        double newMoney = money - seuil;
        world.setMoney(newMoney);

        // modifier le produit en fonction du ratio du cash upgrade
        calcUpgrade(upgrade, product);

        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }

    public void calcUpgrade(PallierType pallier, ProductType p) {
        pallier.setUnlocked(true);
        if (pallier.getTyperatio() == TyperatioType.VITESSE) {
            double vitesse = p.getVitesse();
            vitesse = (int) (vitesse * pallier.getRatio());
            p.setVitesse((int) vitesse);
        }
        if (pallier.getTyperatio() == TyperatioType.GAIN) {
            double revenu = p.getRevenu();
            revenu = revenu * pallier.getRatio();
            p.setRevenu(revenu);
        }
    }

    public PallierType findUnlockByName(World world, String name) {
        PallierType un = null;
        for (PallierType p : world.getAllunlocks().pallier) {
            if (name.equals(p.getName())) {
                un = p;
            }
        }
        return un;
    }

    public Boolean updateAllUnlock(String username, PallierType newAllUnlock) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le allunlock équivalent à celui passé en paramètre
        PallierType allUnlock = findUnlockByName(world, newAllUnlock.getName());
        if (allUnlock == null) {
            return false;
        }
        // débloquer ce cash upgrade
        allUnlock.setUnlocked(true);
        // trouver les produits correspondant au allUnlock
        if (allUnlock.getIdcible() == 0) {
            List<ProductType> products = world.getProducts().getProduct();

            // modifier les produits en fonction du ratio du cash upgrade
            for (ProductType product : products) {
                calcUpgrade(allUnlock, product);
            }
            // sauvegarder les changements au monde
            saveWorldToXml(username, world);
            return true;
        }
        return false;
    }

    public double nombreAnges(World world) throws JAXBException {
        double nombreAnges = world.getTotalangels();
        double angeToClaim = Math.round(150 * Math.sqrt((world.getScore()) / Math.pow(10, 15))) - nombreAnges;
        return angeToClaim;
    }

    public PallierType findAngelByName(World world, String name) {
        PallierType ange = null;
        for (PallierType a : world.getAngelupgrades().pallier) {
            if (name.equals(a.getName())) {
                ange = a;
            }
        }
        return ange;
    }

    public Boolean angelUpgrade(String username, PallierType angel) throws JAXBException{
        World world = getWorld(username);
        PallierType ange = findAngelByName(world, angel.getName());
        if (ange == null) {
            return false;
        }
        // débloquer cet ange
        ange.setUnlocked(true);
        int angels = ange.getSeuil();
        double totalangels = world.getTotalangels();
        double newtotalangel = totalangels - angels;
        if(ange.getTyperatio() == TyperatioType.ANGE) {
            int angeBonus = world.getAngelbonus();
            angeBonus += angeBonus + ange.getRatio();
            world.setAngelbonus(angeBonus);
}
        else{
               updateUpgrade(username, ange);
        }
        world.setActiveangels(newtotalangel);
        saveWorldToXml(username, world);
        return true;
    }
}
