/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.ISIS_Capitalist;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

/**
 *
 * @author emmasalvan
 */
@Path("generic")
public class Webservices {

    Services services;
    //static ArrayList<Long> timeDiff = new ArrayList<>();

    public Webservices() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("product")
    public void putProduct(@Context HttpServletRequest request, ProductType product) throws JAXBException {
        //long timeBefore = System.currentTimeMillis();
        String username = request.getHeader("X-user");
        services.updateProduct(username, product);
       // long timeAfter = System.currentTimeMillis();
//        timeDiff.add(timeAfter - timeBefore);
//        if (timeDiff.size() % 10 == 0) {
//            double sum = 0;
//            for (int i = 0; i < timeDiff.size(); i++) {
//                sum = sum + timeDiff.get(i);
//            }
//        }
    }

    @PUT
    @Path("manager")
    public void putManager(@Context HttpServletRequest request, PallierType manager) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateManager(username, manager);
    }
    
    @PUT
    @Path("upgrade")
    public void putUpgrade(@Context HttpServletRequest request, PallierType upgrade) throws JAXBException{
        String username = request.getHeader("X-user");
        services.updateUpgrade(username, upgrade);
    }

    @PUT
    @Path("world")
    public void deleteWorld(@Context HttpServletRequest request)throws JAXBException{
        String username = request.getHeader("X-user");
        services.deleteWorld(username);
    }
}
