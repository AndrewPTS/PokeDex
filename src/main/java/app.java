package main.java; /**
 * Created by Andrew on 6/21/2017.
 */
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class app extends Application{

    private static Pokedex pokedex;
    private ActionHelper actionHelper = new ActionHelper();
    String pokeTemp = "";
    String routeTemp = "";
    private static HashMap<String, Pokemon> pokeLookup = new HashMap<>();
    private static HashMap<String, ArrayList<String>> routeLookup = new HashMap<>();
    private Scene scene;
    private Pane root = new Pane();
    private TabPane tabPane = new TabPane();
    private Scale scale;
    private ImageView img = new ImageView();
    private Button addPokeBttn = new Button();
    private Button editMon = new Button();
    private Button delMon = new Button();
    private VBox pokeTextArea = new VBox();
    private Text pokeTitleText = new Text();
    private HBox pokeTitleArea = new HBox(16);
    private VBox routeTextArea = new VBox();
    private HBox routeTitleArea = new HBox(16);
    private ScrollPane sp = new ScrollPane();
    private Text routeTitleText = new Text();
    private ComboBox<String> pokeComboBox;
    private FilteredList<String> filteredList;
    private ComboBox<String> routeComboBox;
    private FilteredList<String> filteredList2;
    private ObservableList<String> routeList;
    private TextField nameField = new TextField();
    private TextField routeField = new TextField();
    private double DEFAULT_IMAGE_SCALE = .4;
    private double TEXT_BORDER_SIZE = 10;
    private int DEFAULT_FONT_SIZE = 20;
    private int DEFAULT_TITLE_SIZE = 48;
    private int DEFAULT_TITLE_SPACER = 30;
    private boolean addingToRoute = false;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        importPokemon();
        scale = new Scale(1, 1);
        for (Pokemon poke:pokedex.getPokemon()) {
            pokeLookup.put(poke.getName(), poke);
            String[] pokeRoutes = poke.getRoute().split(", ");
            for (int i = 0; i < pokeRoutes.length; i++) {
                if (!routeLookup.containsKey(pokeRoutes[i])) {
                    routeLookup.put(pokeRoutes[i], new ArrayList<>());
                }
                routeLookup.get(pokeRoutes[i]).add(poke.getName());
            }
        }
        primaryStage.setTitle("Pokedex");

        pokeComboBox = new ComboBox<>();
        ObservableList<String> pokeList = FXCollections.observableList(new ArrayList<>(pokedex.getPokemonNames()));
        Collections.sort(pokeList);
        filteredList = new FilteredList<>(pokeList, p -> true);
        pokeComboBox.setPromptText("Pokemon");
        pokeComboBox.setEditable(true);
        pokeComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {

            final TextField editor = pokeComboBox.getEditor();
            final String selected = pokeComboBox.getSelectionModel().getSelectedItem();
            final int oldSize = filteredList.size();

            Platform.runLater(() -> {
                if (selected == null || !selected.equals(editor.getText())) {

                    filteredList.setPredicate(selectedPoke -> {
                        if (selectedPoke.equals("Missing No")) {
                            return false;
                        } else {
                            return selectedPoke.toUpperCase().startsWith(newValue.toUpperCase());
                        }
                    });
                }
                //to avoid buggy comboboxes
                displayImage(scene, pokeTemp);
                setPokeText(pokeTemp);
                if (oldSize != filteredList.size() && selected == null) {
                    pokeComboBox.hide();
                    pokeComboBox.show();
                }
            });
        });
        filteredList.setPredicate(selectedPoke -> !selectedPoke.equals("Missing No"));
        pokeComboBox.setItems(filteredList);

        routeComboBox = new ComboBox<>();
        ArrayList<String> rList = new ArrayList<>(routeLookup.keySet());
        routeList = FXCollections.observableList(rList);
        filteredList2 = new FilteredList<>(routeList, p -> true);
        routeComboBox.setPromptText("Route");
        routeComboBox.setEditable(true);
        routeComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = routeComboBox.getEditor();
            final String selected = routeComboBox.getSelectionModel().getSelectedItem();
            final int oldSize = filteredList2.size();

            Platform.runLater(() -> {
                if (selected == null || !selected.equals(editor.getText())) {
                    filteredList2.setPredicate(selectedRoute -> {
                        if (selectedRoute.equals("None") || selectedRoute.replace("[\\s]+", "").equals("")) {
                            return false;
                        } else {
                            return selectedRoute.toUpperCase().startsWith(newValue.toUpperCase());
                        }
                    });
                }
                //to avoid buggy comboboxes
                setRouteText(routeTemp);

                if (oldSize != filteredList2.size() && selected == null) {
                    routeComboBox.hide();
                    routeComboBox.show();
                }
            });
        });
        Collections.sort(routeList);
        filteredList2 = new FilteredList<>(routeList, selectedRoute -> {
            if (selectedRoute.equals("None") || selectedRoute.replace("[\\s]+", "").equals("")) {
                return false;
            } else {
                return true;
            }
        });
        routeComboBox.setItems(filteredList2);


        root.setPadding(new Insets(5,0,0,0));
        Tab pokeTab = new Tab();
        pokeTab.setText("Pokemon");
        pokeTab.setClosable(false);
        Tab routeTab = new Tab();
        routeTab.setText("Routes");
        routeTab.setClosable(false);
        Group pokeWindow = new Group();
        pokeWindow.getChildren().add(new Rectangle(1280, 720, Color.RED));
        pokeTab.setContent(pokeWindow);
        Group routeWindow = new Group();
        routeWindow.getChildren().add(new Rectangle(1280, 720, Color.GREEN));
        routeWindow.getChildren().add(routeComboBox);
        routeTab.setContent(routeWindow);
        HBox pokeWindowTopBar = new HBox();
        pokeWindowTopBar.getChildren().add(pokeComboBox);
        addPokeBttn.setText("Add a Pokemon");


        /**
         * -------------------------------------------------
         * -------------------------------------------------
         * THIS IS THE BEGINNING OF THE POKEWINDOW SECTION
         * -------------------------------------------------
         * -------------------------------------------------
         */


        addPokeBttn.setOnAction(action -> {
            Stage addPokeWindow = new Stage();
            VBox form = new VBox(28);
            if (!addingToRoute) {
                nameField.setText("");
                routeField.setText("");
                nameField.setPromptText("Name");
                routeField.setPromptText("Route");
            }
            addingToRoute = false;
            TextField typeField = new TextField();
            typeField.setPromptText("Type");
            TextField guessTypeField = new TextField();
            guessTypeField.setPromptText("Guessed Type");
            TextField weakField = new TextField();
            weakField.setPromptText("Weakness");
            TextField resistField = new TextField();
            resistField.setPromptText("Resistance");
            TextField immuneField = new TextField();
            immuneField.setPromptText("Immune");
            Button submit = new Button("Add");
            Text error = new Text();
            error.setText("This Pokemon already exists!");
            error.setVisible(false);
            Button viewButton = new Button("View");
            viewButton.setVisible(false);
            submit.setOnAction(addAction -> {

                nameField.setText(capitalize(nameField.getText()));
                routeField.setText(capitalizeWord(routeField.getText()));
                String[] pokeRoutes = routeField.getText().split(", ");
                for (int i = 0; i < pokeRoutes.length; i++) {
                    if (!routeList.contains(pokeRoutes[i]) && !pokeRoutes[i].equals("")) {
                        routeList.add(routeField.getText());
                    }
                }
                Collections.sort(filteredList2.getSource());
                if (actionHelper.add(nameField.getText(), routeField.getText(), typeField.getText(), guessTypeField.getText(),
                        weakField.getText(), resistField.getText(), immuneField.getText()) == 0) {

                    pokeList.add(nameField.getText());
                    Collections.sort(filteredList.getSource());
                    addPokeWindow.close();
                    tabPane.getSelectionModel().select(pokeTab);
                    pokeComboBox.getSelectionModel().select(nameField.getText());

                } else {
                    error.setVisible(true);
                    viewButton.setVisible(true);
                }
            });
            viewButton.setOnAction(viewAction -> {
                addPokeWindow.close();
                tabPane.getSelectionModel().select(pokeTab);
                pokeComboBox.getSelectionModel().select(nameField.getText());
            });
            VBox errorMessage = new VBox(14);
            errorMessage.getChildren().addAll(error, viewButton);
            errorMessage.setAlignment(Pos.CENTER);
            VBox centeredButton = new VBox(44);
            centeredButton.getChildren().addAll(errorMessage, submit);
            centeredButton.setAlignment(Pos.CENTER);
            form.getChildren().addAll(nameField, routeField, typeField, guessTypeField, weakField, resistField, immuneField, centeredButton);
            Scene addPokeScene = new Scene(form, 320, 540);
            addPokeWindow.setScene(addPokeScene);
            addPokeWindow.show();
        });

        pokeTitleArea.setAlignment(Pos.CENTER_LEFT);
        editMon.setText("Edit");
        editMon.setOnAction(action -> {

            pokeComboBox.getSelectionModel().select(pokeComboBox.getEditor().getText());
            Pokemon poke = pokeLookup.get(pokeComboBox.getSelectionModel().getSelectedItem());
            Stage addPokeWindow = new Stage();
            VBox form = new VBox(48);
            TextField nameField = new TextField();
            nameField.setText(poke.getName());
            TextField routeField = new TextField();
            if (poke.getRoute().equals("")) {
                routeField.setPromptText("Route");
            } else {
                routeField.setText(poke.getRoute());
            }
            TextField typeField = new TextField();
            if (poke.getType().equals("")) {
                typeField.setPromptText("Type");
            } else {
                typeField.setText(poke.getType());
            }
            TextField guessTypeField = new TextField();
            if (poke.getGuesstype().equals("")) {
                guessTypeField.setPromptText("Guessed Type");
            } else {
                guessTypeField.setText(poke.getGuesstype());
            }
            TextField weakField = new TextField();
            if (poke.getWeakness().equals("")) {
                weakField.setPromptText("Weakness");
            } else {
                weakField.setText(poke.getWeakness());
            }
            TextField resistField = new TextField();
            if (poke.getResistant().equals("")) {
                resistField.setPromptText("Resistance");
            } else {
                resistField.setText(poke.getResistant());
            }
            TextField immuneField = new TextField();
            if (poke.getNegated().equals("")) {
                immuneField.setPromptText("Immune");
            } else {
                immuneField.setText(poke.getNegated());
            }
            Button submit = new Button("Edit");
            submit.setOnAction(addAction -> {
                boolean nameChange = !poke.getName().equals(nameField.getText());
                String oldName = poke.getName();

                nameField.setText(capitalize(nameField.getText()));
                routeField.setText(capitalizeWord(routeField.getText()));
                String[] pokeRoutes = routeField.getText().split(", ");
                for (int i = 0; i < pokeRoutes.length; i++) {
                    if (!routeList.contains(pokeRoutes[i]) && !pokeRoutes[i].equals("")) {
                        routeList.add(routeField.getText());
                    }
                }
                String temp = routeComboBox.getSelectionModel().getSelectedItem();
                Collections.sort(filteredList2.getSource());
                routeComboBox.getSelectionModel().select(temp);
                actionHelper.edit(poke.getName(), nameField.getText(), routeField.getText(), typeField.getText(), guessTypeField.getText(),
                        weakField.getText(), resistField.getText(), immuneField.getText());
                addPokeWindow.close();
                if (nameChange) {
                    pokeTemp = nameField.getText();
                    pokeList.add(nameField.getText());
                    pokeList.remove(oldName);
                    Collections.sort(filteredList.getSource());
                    tabPane.getSelectionModel().select(pokeTab);
                    pokeComboBox.getSelectionModel().select(nameField.getText());
                } else {
                    setPokeText(pokeComboBox.getSelectionModel().getSelectedItem());
                }
            });

            VBox centeredButton = new VBox(44);
            centeredButton.getChildren().add(submit);
            centeredButton.setAlignment(Pos.CENTER);
            form.getChildren().addAll(nameField, routeField, typeField, guessTypeField, weakField, resistField, immuneField, centeredButton);
            Scene addPokeScene = new Scene(form, 320, 540);
            addPokeWindow.setScene(addPokeScene);
            addPokeWindow.show();
        });

        delMon.setText("Delete this Pokemon");
        delMon.setOnAction(event -> {

            pokeComboBox.getSelectionModel().select(pokeComboBox.getEditor().getText());
            Stage doubleCheck = new Stage();
            VBox form = new VBox(20);
            Text message = new Text("Are you sure?");
            message.setFont(Font.font( "Serif", FontWeight.BOLD, DEFAULT_TITLE_SIZE));
            HBox choice = new HBox(50);
            Button yes = new Button();
            Button no = new Button();
            yes.setText("Yes");
            yes.setOnAction(action -> {
                String[] pokeRoutes = pokeLookup.get(pokeComboBox.getSelectionModel().getSelectedItem()).getRoute().split(", ");
                for (int i = 0; i < pokeRoutes.length; i++) {
                    routeLookup.get(pokeRoutes[i]).remove(pokeLookup.get(pokeComboBox.getSelectionModel().getSelectedItem()).getName());
                }
                actionHelper.removeUnusedRoutes();
                pokedex.removePoke(pokeLookup.get(pokeComboBox.getSelectionModel().getSelectedItem()));
                pokeLookup.remove(pokeComboBox.getSelectionModel().getSelectedItem());
                pokedex.exportPokemon();
                pokeList.remove(pokeComboBox.getSelectionModel().getSelectedItem());
                pokeComboBox.getSelectionModel().clearSelection();
                doubleCheck.close();
                pokeTemp = "";
            });
            no.setText("No");
            no.setOnAction(action -> {
                doubleCheck.close();
            });
            choice.getChildren().addAll(yes, no);
            choice.setAlignment(Pos.CENTER);
            form.getChildren().addAll(message, choice);
            form.setAlignment(Pos.CENTER);
            Scene scene = new Scene(form, 480, 120);
            doubleCheck.setScene(scene);
            doubleCheck.show();

        });


        pokeWindowTopBar.getChildren().add(addPokeBttn);
        pokeWindow.getChildren().add(pokeWindowTopBar);
        pokeWindow.getChildren().add(img);

        /**
         * -------------------------------------------------
         * -------------------------------------------------
         * THIS IS THE BEGINNING OF THE ROUTEWINDOW SECTION
         * -------------------------------------------------
         * -------------------------------------------------
         */



        tabPane.getTabs().add(pokeTab);
        tabPane.getTabs().add(routeTab);
        root.getChildren().add(tabPane);


        tabPane.getSelectionModel().selectedIndexProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                if (newValue.equals(1)) {
                    if (routeComboBox.getSelectionModel().getSelectedItem() != null) {
                        setRouteText(routeComboBox.getSelectionModel().getSelectedItem());
                    }
                    routeWindow.setVisible(true);
                    pokeWindow.setVisible(false);
                } else {
                    if (pokeComboBox.getSelectionModel().getSelectedItem() != null) {
                        setPokeText(pokeComboBox.getSelectionModel().getSelectedItem());
                    }
                    routeWindow.setVisible(false);
                    pokeWindow.setVisible(true);
                }
            }
        });


        scene = new Scene(root, 1280, 720);
        routeWindow.setVisible(false);
        pokeWindow.getChildren().add(pokeTextArea);
        routeWindow.getChildren().add(routeTextArea);
        primaryStage.setScene(scene);
        primaryStage.show();
        displayImage(scene);
        setTextArea(scene, pokeTextArea);
        setTextArea(scene, routeTextArea);
        setPokeText();
        scaleWindow(scene, root);


        pokeComboBox.setOnAction(event -> {
            if (pokeComboBox.getSelectionModel().getSelectedItem() != null) {
                pokeTemp = pokeComboBox.getSelectionModel().getSelectedItem();
                displayImage(scene, pokeComboBox.getSelectionModel().getSelectedItem());
                setPokeText(pokeComboBox.getSelectionModel().getSelectedItem());
            } else {
                displayImage(scene);
                setPokeText("");
                pokeComboBox.getSelectionModel().clearSelection();
            }
        });

        routeComboBox.setOnAction(event -> {

            if (routeComboBox.getSelectionModel().getSelectedItem() != null) {
                routeTemp = routeComboBox.getSelectionModel().getSelectedItem();
            }
            try {
                setRouteText(routeComboBox.getSelectionModel().getSelectedItem());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    private class ActionHelper {

        private int add(String name, String route, String type, String guesstype, String weakness, String resistant,
                               String negated) {
            name = capitalize(name);
            if (!isExisting(name)){
                Pokemon newPoke = new Pokemon(name, route, type, guesstype, weakness, resistant, negated, IdPatcher.getId(name));
                pokedex.addPoke(newPoke);
                pokeLookup.put(name, newPoke);
                String[] pokeRoutes = newPoke.getRoute().split(", ");
                for (int i = 0; i < pokeRoutes.length; i++) {
                    if (!routeLookup.containsKey(pokeRoutes[i])) {
                        routeLookup.put(pokeRoutes[i], new ArrayList<>());
                    }
                    if (!routeLookup.get(pokeRoutes[i]).contains(newPoke.getName())) {
                        routeLookup.get(pokeRoutes[i]).add(newPoke.getName());
                    }
                }
                Collections.sort(filteredList2.getSource());
                pokedex.exportPokemon();
                pokeTemp = nameField.getText();
                System.out.println(pokeTemp);
                return 0;
            } else {
                return 1;
            }
        }

        private void removeUnusedRoutes() {

            for (String route:routeLookup.keySet()) {
                if (routeLookup.get(route).size() == 0) {
                    routeList.remove(route);
                }
            }
        }

        private void edit(String editPoke, String newName, String route, String type, String guesstype, String weakness,
                                 String resistant, String negated) {
            editPoke = capitalize(editPoke);
            int chosenPoke = 0;
            for (int i = 0; i < pokedex.getPokemon().size(); i++) {
                if (pokedex.getPokemon().get(i).getName().equals(editPoke)) {
                    chosenPoke = i;
                    i+=10000;
                } else if (i == pokedex.getPokemon().size()-1) {
                    chosenPoke = 10000;
                }
            }
            if (!(chosenPoke == 10000)) {
                Pokemon poke = pokeLookup.get(editPoke);
                poke.setName(newName);
                poke.setId(IdPatcher.getId(newName));
                String[] pokeRoutes = poke.getRoute().split(", ");
                for (int i = 0; i < pokeRoutes.length; i++) {
                    routeLookup.get(pokeRoutes[i]).remove(poke.getName());
                }
                poke.setRoute(route);
                poke.setType(type);
                poke.setGuesstype(guesstype);
                poke.setWeakness(weakness);
                poke.setResistant(resistant);
                poke.setNegated(negated);
                if (!editPoke.equals(newName)) {
                    pokeLookup.remove(editPoke);
                    pokeLookup.put(poke.getName(), poke);
                }
                pokeRoutes = poke.getRoute().split(", ");
                for (int i = 0; i < pokeRoutes.length; i++) {
                    if (!routeLookup.containsKey(pokeRoutes[i])) {
                        routeLookup.put(pokeRoutes[i], new ArrayList<>());
                    }
                    if (!routeLookup.get(pokeRoutes[i]).contains(poke.getName())) {
                        routeLookup.get(pokeRoutes[i]).add(poke.getName());
                    }
                }
                removeUnusedRoutes();
                Collections.sort(filteredList2.getSource());
                if (!routeList.contains(routeTemp)) {
                    routeTemp = "";
                    routeComboBox.getSelectionModel().clearSelection();
                }
            }
            pokedex.exportPokemon();
        }

        private void addToRoute(String route, String poke) {
            if (pokeLookup.get(poke).getRoute().equals("")) {
                pokeLookup.get(poke).setRoute(route);
            } else {
                pokeLookup.get(poke).setRoute(pokeLookup.get(poke).getRoute() + ", " + route);
            }
            String[] pokeRoutes = pokeLookup.get(poke).getRoute().split(", ");
            for (int i = 0; i < pokeRoutes.length; i++) {
                if (!routeLookup.containsKey(pokeRoutes[i])) {
                    routeLookup.put(pokeRoutes[i], new ArrayList<>());
                }
                if (!routeLookup.get(pokeRoutes[i]).contains(poke)) {
                    routeLookup.get(pokeRoutes[i]).add(poke);
                }
            }
            pokedex.exportPokemon();
        }

    }

    private void setTextArea(Scene scene, VBox textArea) {
        textArea.setLayoutX(TEXT_BORDER_SIZE*4);
        textArea.setLayoutY(TEXT_BORDER_SIZE + pokeComboBox.getHeight());
        textArea.setMaxWidth(scene.getWidth()-img.getFitWidth()-TEXT_BORDER_SIZE*4);
        textArea.setMinWidth(scene.getWidth()-img.getFitWidth()-TEXT_BORDER_SIZE*4);
        textArea.setPrefWidth(scene.getWidth()-img.getFitWidth()-TEXT_BORDER_SIZE*4);
        textArea.setMaxHeight(scene.getHeight()-routeComboBox.getHeight()-routeTitleText.getBoundsInLocal().getHeight()-DEFAULT_TITLE_SPACER-TEXT_BORDER_SIZE*2);
        textArea.setMinHeight(scene.getHeight()-routeComboBox.getHeight()-routeTitleText.getBoundsInLocal().getHeight()-DEFAULT_TITLE_SPACER-TEXT_BORDER_SIZE*2);
        textArea.setPrefHeight(scene.getHeight()-routeComboBox.getHeight()-routeTitleText.getBoundsInLocal().getHeight()-DEFAULT_TITLE_SPACER-TEXT_BORDER_SIZE*2);

    }

    private void setPokeText() {
        setPokeText(pokedex.getPokemon().get(0).getName());
    }

    private void setPokeText(String poke) {
        System.out.println(poke);
        img.setFitHeight(scene.getWidth() * DEFAULT_IMAGE_SCALE);
        img.setFitWidth(scene.getWidth() * DEFAULT_IMAGE_SCALE);
        img.setX(scene.getWidth()-img.getFitWidth());
        img.setY(0);
        img.setPreserveRatio(true);

        pokeTitleText.setText("");
        pokeTextArea.getChildren().remove(0, pokeTextArea.getChildren().size());
        pokeTitleArea.getChildren().remove(0, pokeTitleArea.getChildren().size());
        poke = capitalize(poke);
        pokeTitleText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE));
        VBox pokeInfo = new VBox(28);

        if (pokeLookup.containsKey(poke) && !poke.equals("Missing No")) {
            pokeTitleText.setText(poke);
            pokeTitleArea.getChildren().addAll(pokeTitleText, editMon, delMon);

            VBox pokeRoute = new VBox(10);
            Text pokeRouteTitle = new Text();
            pokeRouteTitle.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE/2));
            if (pokeLookup.get(poke).getRoute().contains(",")) {
                pokeRouteTitle.setText("  Routes");
            } else {
                pokeRouteTitle.setText("  Route");
            }
            Text pokeRouteText = new Text("    " + pokeLookup.get(poke).getRoute());
            pokeRouteText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_FONT_SIZE));
            pokeRoute.getChildren().addAll(pokeRouteTitle, pokeRouteText);
            VBox pokeType = new VBox(10);
            Text pokeTypeTitle = new Text();
            pokeTypeTitle.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE/2));
            if (pokeLookup.get(poke).getType().contains(",")) {
                pokeTypeTitle.setText("  Types");
            } else {
                pokeTypeTitle.setText("  Type");
            }
            Text pokeTypeText = new Text("    " + pokeLookup.get(poke).getType());
            pokeTypeText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_FONT_SIZE));
            pokeType.getChildren().addAll(pokeTypeTitle, pokeTypeText);
            VBox pokeGuess = new VBox(10);
            Text pokeGuessTitle = new Text();
            pokeGuessTitle.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE/2));
            if (pokeLookup.get(poke).getGuesstype().contains(",")) {
                pokeGuessTitle.setText("  Guessed Types");
            } else {
                pokeGuessTitle.setText("  Guessed Type");
            }
            Text pokeGuessText = new Text("    " + pokeLookup.get(poke).getGuesstype());
            pokeGuessText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_FONT_SIZE));
            pokeGuess.getChildren().addAll(pokeGuessTitle, pokeGuessText);
            VBox pokeWeak = new VBox(10);
            Text pokeWeakTitle = new Text();
            pokeWeakTitle.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE/2));
            if (pokeLookup.get(poke).getWeakness().contains(",")) {
                pokeWeakTitle.setText("  Weaknesses");
            } else {
                pokeWeakTitle.setText("  Weakness");
            }
            Text pokeWeakText = new Text("    " + pokeLookup.get(poke).getWeakness());
            pokeWeakText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_FONT_SIZE));
            pokeWeak.getChildren().addAll(pokeWeakTitle, pokeWeakText);
            VBox pokeRes = new VBox(10);
            Text pokeResTitle = new Text();
            pokeResTitle.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE/2));
            if (pokeLookup.get(poke).getResistant().contains(",")) {
                pokeResTitle.setText("  Resistances");
            } else {
                pokeResTitle.setText("  Resistance");
            }
            Text pokeResText = new Text("    " + pokeLookup.get(poke).getResistant());
            pokeResText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_FONT_SIZE));
            pokeRes.getChildren().addAll(pokeResTitle, pokeResText);
            VBox pokeIm = new VBox(10);
            Text pokeImTitle = new Text();
            pokeImTitle.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE/2));
            if (pokeLookup.get(poke).getNegated().contains(",")) {
                pokeImTitle.setText("  Immunities");
            } else {
                pokeImTitle.setText("  Immunity");
            }
            Text pokeImText = new Text("    " + pokeLookup.get(poke).getNegated());
            pokeImText.setFont(Font.font( "Serif", FontWeight.EXTRA_BOLD, DEFAULT_FONT_SIZE));
            pokeIm.getChildren().addAll(pokeImTitle, pokeImText);
            pokeInfo.getChildren().addAll(pokeRoute, pokeType, pokeGuess, pokeWeak, pokeRes, pokeIm);
        }
        pokeTextArea.getChildren().addAll(new VBox(DEFAULT_TITLE_SPACER, pokeTitleArea, pokeInfo));
    }

    private void setRouteText(String rout) {
        routeTitleText.setText("");
        routeTextArea.getChildren().remove(0, routeTextArea.getChildren().size());
        routeTitleArea.getChildren().remove(0, routeTitleArea.getChildren().size());
        final String route = capitalizeWord(rout);
        if (!route.equals("None")) {
            routeTitleText.setFont(Font.font("Serif", FontWeight.EXTRA_BOLD, DEFAULT_TITLE_SIZE));
            routeTitleText.setText(route);
            Button addToRouteBttn = new Button("Add a Pokemon to this route");
            addToRouteBttn.setText("Add a Pokemon to this route");
            addToRouteBttn.setOnAction(action -> {
                Stage prompt = new Stage();
                VBox form = new VBox(20);
                TextField field = new TextField();
                field.setFont(Font.font("Serif", FontWeight.BOLD, DEFAULT_TITLE_SIZE));
                field.setPromptText("Enter name of Pokemon you would like to add to this route");
                Button submit = new Button("Submit");
                submit.setOnAction(e -> {
                    final String poke = capitalize(field.getText());
                    if (routeLookup.get(route).contains(poke)) {
                        prompt.close();
                    } else if (pokeLookup.containsKey(poke)) {
                        actionHelper.addToRoute(route, poke);
                        prompt.close();
                        setRouteText(route);
                    } else {
                        prompt.close();
                        Stage dbblCheck = new Stage();
                        VBox form2 = new VBox(20);
                        Text message = new Text("This pokemon does not exist.\nWould you like to add it to the Pokedex?");
                        message.setFont(Font.font("Serif", FontWeight.BOLD, DEFAULT_TITLE_SIZE/2));
                        HBox bttns = new HBox(40);
                        Button y = new Button("Yes");
                        y.setOnAction(yes -> {
                            dbblCheck.close();
                            nameField.setText(poke);
                            routeField.setText(route);
                            addingToRoute = true;
                            tabPane.getSelectionModel().select(0);
                            addPokeBttn.fire();
                        });
                        Button n = new Button("No");
                        n.setOnAction(no -> {
                            dbblCheck.close();
                        });
                        bttns.getChildren().addAll(y, n);
                        bttns.setAlignment(Pos.CENTER);
                        form2.getChildren().addAll(message, bttns);
                        form2.setAlignment(Pos.CENTER);
                        Scene scene2 = new Scene(form2, 480, 240);
                        dbblCheck.setScene(scene2);
                        dbblCheck.show();
                    }
                });
                form.getChildren().addAll(field, submit);
                form.setAlignment(Pos.CENTER);
                Scene scene = new Scene(form ,480, 240);
                prompt.setScene(scene);
                prompt.show();
            });
            VBox routeInfo = new VBox(16);
            if (routeLookup.keySet().contains(route)) {
                for (String poke : routeLookup.get(route)) {
                    HBox pokeLink = new HBox(20);
                    Text redirMessage = new Text(poke);
                    Hyperlink redirect = new Hyperlink();
                    redirMessage.setFont(Font.font("Serif", DEFAULT_TITLE_SIZE));
                    redirect.setOnAction(event -> {
                        tabPane.getSelectionModel().select(0);
                        pokeComboBox.getSelectionModel().select(poke);
                    });
                    pokeLink.getChildren().add(redirMessage);
                    pokeLink.getChildren().add(0, createThumb(redirMessage.getLayoutBounds().getHeight() * 4, poke));
                    pokeLink.setAlignment(Pos.CENTER);
                    redirect.setGraphic(pokeLink);
                    routeInfo.getChildren().add(redirect);
                }
            }
            sp.setContent(routeInfo);
            if (!(routeTitleText.getText().equals("") || (routeTitleText == null))) {
                routeTitleArea.getChildren().addAll(routeTitleText, addToRouteBttn);
                routeTitleArea.setAlignment(Pos.CENTER_LEFT);
                routeTextArea.getChildren().add(new VBox(DEFAULT_TITLE_SPACER, routeTitleArea, sp));
            }
        }



    }

    private void displayImage(Scene scene) {
        displayImage(scene, pokedex.getPokemon().get(0).getName());
    }

    private void displayImage(Scene scene, String poke) {
        try {
            poke = capitalize(poke);
            Path temp  = Files.createTempFile("resource-", ".tmp");
            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("main-sprites/" + pokeLookup.get(poke).getId() + ".png"), temp, StandardCopyOption.REPLACE_EXISTING);
            } catch (NullPointerException e) {
                Files.copy(getClass().getClassLoader().getResourceAsStream("main-sprites/" + "0.png"), temp, StandardCopyOption.REPLACE_EXISTING);
            }
            Image thumbnail = new Image(new FileInputStream(temp.toFile()));
            img.setImage(thumbnail);
            Scale invert = new Scale(1/scale.getX(), 1/scale.getY());
            img.getTransforms().setAll(invert);
        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ImageView createThumb(double size, String poke) {
        try {
            poke = capitalize(poke);
            Path temp = Files.createTempFile("resource-", ".tmp");
            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("main-sprites/" + pokeLookup.get(poke).getId() + ".png"), temp, StandardCopyOption.REPLACE_EXISTING);
            } catch (NullPointerException e) {
                Files.copy(getClass().getClassLoader().getResourceAsStream("main-sprites/" + "0.png"), temp, StandardCopyOption.REPLACE_EXISTING);
            }
            ImageView thumb = new ImageView();
            thumb.setImage(new Image(new FileInputStream(temp.toFile())));
            thumb.setFitWidth(size); thumb.setFitHeight(size);
            thumb.setPreserveRatio(true);
            return thumb;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void scaleWindow(final Scene scene, final Pane contentPane) {
        final double initWidth  = scene.getWidth();
        final double initHeight = scene.getHeight();
        final double ratio      = initWidth / initHeight;

        SceneSizeChangeListener sizeListener = new SceneSizeChangeListener(scene, ratio, initHeight, initWidth, contentPane);
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
    }

    private class SceneSizeChangeListener implements javafx.beans.value.ChangeListener<Number> {

        private final Scene scene;
        private final double ratio;
        private final double initHeight;
        private final double initWidth;
        private final Pane contentPane;


        public SceneSizeChangeListener(Scene scene, double ratio, double initHeight, double initWidth, Pane contentPane) {
            this.scene = scene;
            this.ratio = ratio;
            this.initHeight = initHeight;
            this.initWidth = initWidth;
            this.contentPane = contentPane;
        }

        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {

            final double newWidth = scene.getWidth();
            final double newHeight = scene.getHeight();

            double scaleFactor =
                    newWidth / newHeight > ratio

                            ? newHeight / initHeight
                            : newWidth / initWidth;


            if (scaleFactor >= 1) {
                scale = new Scale(newWidth / initWidth, newHeight / initHeight);
                scale.setPivotX(0);
                scale.setPivotY(0);
                scene.getRoot().getTransforms().setAll(scale);

                contentPane.setPrefWidth(newWidth / scaleFactor);
                contentPane.setPrefHeight(newHeight / scaleFactor);

            } else if (scaleFactor < 1) {
                scale = new Scale(newWidth / initWidth,newHeight / initHeight);
                scale.setPivotX(0);
                scale.setPivotY(0);
                scene.getRoot().getTransforms().setAll(scale);

                contentPane.setPrefWidth(Math.max(initWidth, newWidth));
                contentPane.setPrefHeight(Math.max(initHeight, newHeight));
            } else {
                scale = new Scale(newHeight / initHeight, newWidth / initWidth);
                scale.setPivotX(0);
                scale.setPivotY(0);
                scene.getRoot().getTransforms().setAll(scale);

                contentPane.setPrefWidth(Math.max(initWidth, newWidth));
                contentPane.setPrefHeight(Math.max(initHeight, newHeight));
            }

        }
    }

    private static String capitalize(String lower) {
        try {
            String[] words = lower.split("\\s+");
            ArrayList<String> capitalized = new ArrayList<>();
            for (String word : words) {
                try {
                    String first = word.substring(0, 1);
                    capitalized.add(first.toUpperCase() + word.substring(1));
                } catch (StringIndexOutOfBoundsException e) {
                    return "";
                }
            }
            String caps = "";
            for (String w : capitalized) {
                caps += w + " ";
            }
            caps = caps.substring(0, caps.length() - 1);
            return caps;
        } catch (NullPointerException e) {}
        return "";
    }

    private static String capitalizeWord(String lower) {
        try {
            String caps;
            try {
                String first = lower.substring(0, 1);
                caps = first.toUpperCase() + lower.substring(1);
            } catch (StringIndexOutOfBoundsException e) {
                return "";
            }
            return caps;
        } catch (NullPointerException e) {}
        return "";
    }


    /**


     public static void routelist(String route) {

     for (Pokemon poke:pokedex.getPokemon()) {
     String[] listOfRoutes = poke.getRoute().split(",");
     for (int i = 0; i < listOfRoutes.length; i++) {
     if (i == 0) {
     if (listOfRoutes[i].equals(route)) {
     System.out.println(poke);
     }
     } else {
     if (listOfRoutes[i].substring(1).equals(route)) {
     System.out.println(poke);
     }
     }
     }


     }

     }

     public static void list() {
     for (Pokemon poke:pokedex.getPokemon()) {
     System.out.println(poke);
     }
     }

     public static void append() {
     System.out.println("Enter the name of the Pokemon you would like to edit:\t");
     String editPoke = input.nextLine();
     editPoke = capitalize(editPoke);
     int chosenPoke = 0;
     for (int i = 0; i < pokedex.getPokemon().size(); i++) {
     if (pokedex.getPokemon().get(i).getName().equals(editPoke)) {
     chosenPoke = i;
     i+=10000;
     } else if (i == pokedex.getPokemon().size()-1) {
     chosenPoke = 10000;
     }
     }
     if (chosenPoke == 10000 && chosenPoke != 0) {
     System.out.println("That Pokemon is not in the Pokedex!");
     } else {
     appendSpecific(chosenPoke);
     }
     }


     public static void editSpecific(int chosenPoke) {
     System.out.println("What would you like to edit:\nname\troute\ttype\tguesstype\tresistant\tnegated\tweakness\tback");
     String decision = input.nextLine();
     System.out.println("Please type the new value");
     String value = input.nextLine();
     if (decision.equals("name")) {
     pokedex.getPokemon().get(chosenPoke).setName(value);
     pokedex.exportPokemon();
     } else if (decision.equals("route")) {
     pokedex.getPokemon().get(chosenPoke).setRoute(value);
     pokedex.exportPokemon();
     } else if (decision.equals("type")) {
     pokedex.getPokemon().get(chosenPoke).setType(value);
     pokedex.exportPokemon();
     } else if (decision.equals("guesstype")) {
     pokedex.getPokemon().get(chosenPoke).setGuesstype(value);
     pokedex.exportPokemon();
     } else if (decision.equals("resistant")) {
     pokedex.getPokemon().get(chosenPoke).setResistant(value);
     pokedex.exportPokemon();
     } else if (decision.equals("negated")) {
     pokedex.getPokemon().get(chosenPoke).setNegated(value);
     pokedex.exportPokemon();
     } else if (decision.equals("weakness")) {
     pokedex.getPokemon().get(chosenPoke).setWeakness(value);
     pokedex.exportPokemon();
     } else if (decision.equals("back")) {
     edit();
     } else {
     editSpecific(chosenPoke);
     }
     }


     public static void appendSpecific(int chosenPoke) {
     System.out.println("What would you like to edit:\troute\ttype\tguesstype\tresistant\tnegated\tweakness\tback");
     String decision = input.nextLine();
     System.out.println("Please type the value you would like to append");
     String value = input.nextLine();
     if (decision.equals("route")) {
     pokedex.getPokemon().get(chosenPoke).setRoute(pokedex.getPokemon().get(chosenPoke).getRoute()+", " + value);
     pokedex.exportPokemon();
     } else if (decision.equals("type")) {
     pokedex.getPokemon().get(chosenPoke).setType(pokedex.getPokemon().get(chosenPoke).getType()+", " + value);
     pokedex.exportPokemon();
     } else if (decision.equals("guesstype")) {
     pokedex.getPokemon().get(chosenPoke).setGuesstype(pokedex.getPokemon().get(chosenPoke).getGuesstype()+", " + value);
     pokedex.exportPokemon();
     } else if (decision.equals("resistant")) {
     pokedex.getPokemon().get(chosenPoke).setResistant(pokedex.getPokemon().get(chosenPoke).getResistant()+", " + value);
     pokedex.exportPokemon();
     } else if (decision.equals("negated")) {
     pokedex.getPokemon().get(chosenPoke).setNegated(pokedex.getPokemon().get(chosenPoke).getNegated()+", "+value);
     pokedex.exportPokemon();
     } else if (decision.equals("weakness")) {
     pokedex.getPokemon().get(chosenPoke).setWeakness(pokedex.getPokemon().get(chosenPoke).getWeakness()+", "+value);
     pokedex.exportPokemon();
     } else if (decision.equals("back")) {
     edit();
     } else {
     appendSpecific(chosenPoke);
     }
     }

     public static void view() {
     System.out.println("Enter the name of the Pokemon you would like to view:\t");
     String viewPoke = input.nextLine();
     viewPoke = capitalize(viewPoke);
     int chosenPoke = 0;
     for (int i = 0; i < pokedex.getPokemon().size(); i++) {
     if (pokedex.getPokemon().get(i).getName().equals(viewPoke)) {
     chosenPoke = i;
     i+=10000;
     } else if (i == pokedex.getPokemon().size()-1) {
     chosenPoke = 10000;
     }
     }
     if (chosenPoke == 10000) {
     System.out.println("That Pokemon is not in the Pokedex!");
     } else {
     System.out.println(pokedex.getPokemon().get(chosenPoke).toString());
     }

     }
     */


    private static void importPokemon() {
        try {
            File pokeList = new File("./dex/Pokedex");
            JAXBContext pokeContext = JAXBContext.newInstance(Pokedex.class);
            Unmarshaller pokeUnmarshaller = pokeContext.createUnmarshaller();
            pokedex = (Pokedex)pokeUnmarshaller.unmarshal(pokeList);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static boolean isExisting(String name) {
        for (Pokemon poke:pokedex.getPokemon()) {
            if (poke.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}