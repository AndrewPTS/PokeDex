package com.Andrew.PokeProject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by Andrew on 8/2/2018.
 */
@XmlRootElement(name="pokemon")
public class Pokemon implements Comparable<Pokemon> {

    private String name;
    private String route;
    private String type;
    private String guesstype;
    private String resistant;
    private String negated;
    private String weakness;
    private String id;

    public Pokemon() {

    }

    public Pokemon(String name, String route, String type, String guesstype, String resistant, String negated, String weakness, String id) {
        this.name = name;
        this.route = route;
        this.type = type;
        this.guesstype = guesstype;
        this.resistant = resistant;
        this.negated = negated;
        this.weakness = weakness;
        this.id = id;
    }

    public int compareTo(Pokemon poke) {
        return this.getId().compareTo(poke.getId());
    }

    @XmlElement(name="name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name="route")
    public void setRoute(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    @XmlElement(name="type")
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @XmlElement(name="guesstype")
    public void setGuesstype(String guesstype) {
        this.guesstype = guesstype;
    }

    public String getGuesstype() {
        return guesstype;
    }

    @XmlElement(name="resistant")
    public void setResistant(String resistant) {
        this.resistant = resistant;
    }

    public String getResistant() {
        return resistant;
    }

    @XmlElement(name="negated")
    public void setNegated(String negated) {
        this.negated = negated;
    }

    public String getNegated() {
        return negated;
    }

    @XmlElement(name="weakness")
    public void setWeakness(String weakness) {
        this.weakness = weakness;
    }

    public String getWeakness() {
        return weakness;
    }

    @XmlElement(name="id")
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
