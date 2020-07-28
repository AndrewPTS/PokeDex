package main.java; /**
 * Created by Andrew on 8/2/2018.
 */

import javafx.collections.FXCollections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@XmlRootElement (name="pokedex")
public class Pokedex {

    @XmlElement(name="pokemon", type= Pokemon.class)
    private ArrayList<Pokemon> pokemon = new ArrayList<>();

    public Pokedex() {

    }

    public Pokedex(ArrayList<Pokemon> pokemon) {
        this.pokemon = pokemon;
    }

    public ArrayList<Pokemon> getPokemon() {
        return pokemon;
    }

    public ArrayList<String> getPokemonNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Pokemon poke:pokemon) {
            names.add(poke.getName());
        }
        return names;
    }

    public void addPoke(Pokemon newPoke) {
        pokemon.add(newPoke);
    }

    public void removePoke(Pokemon poke) {
        pokemon.remove(poke);
    }

    public void exportPokemon() {
        try {
            File pokeList = new File("./dex/Pokedex");
            JAXBContext pokeContext = JAXBContext.newInstance(Pokedex.class);
            Marshaller pokeMarshaller = pokeContext.createMarshaller();
            pokeMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            pokeMarshaller.marshal(this, pokeList);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

}