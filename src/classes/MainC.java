package classes;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;

public class MainC {
    // Variables (UI elements)

    @FXML
    private ListView<Object> preview;
    @FXML
    private TextField product_name = new TextField();
    @FXML
    private TextField unit_price = new TextField();
    @FXML
    private TextField quantity = new TextField();
    @FXML
    private TextField total = new TextField();
    @FXML
    Product[] inventory;
    @FXML
    private ChoiceBox<String> ch;
    // Methods0

    public void initialize(){
        System.out.println("App launched");
        //reload_inventory();
        // The following TextFields only accept numbers
        unit_price.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    unit_price.setText(newValue.replaceAll("\\D", ""));
                }
            }
        });
        quantity.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    quantity.setText(newValue.replaceAll("\\D", ""));
                }
            }
        });
    }
    @FXML
    protected void addToReceipt() {
        if (!Objects.equals(product_name.getText(), "") && !Objects.equals(total.getText(), "")) {
            Button big = new Button("Delete");
            big.setOnAction(new EventHandler<ActionEvent>()
            {
                public void handle(ActionEvent e)
                {
                   preview.getItems().remove(preview.getItems().indexOf(big)-1);
                   preview.getItems().remove(big);

                }
            });
            preview.getItems().add(String.join("-", product_name.getText().split(" ")) + "\n" + Integer.parseInt(quantity.getText()) + " " + Integer.parseInt(unit_price.getText()) + " " + (Integer.parseInt(quantity.getText()) * Integer.parseInt(unit_price.getText())));
            preview.getItems().add(big);
            // Reset view
            {
                product_name.setText("");
                unit_price.setText("");
                quantity.setText("");
                total.setText("");
            }
        }
    }
    @FXML
    private void setTotal(){
        if(!Objects.equals(unit_price.getText(), "") && !Objects.equals(quantity.getText(), "")){
            int a = Integer.parseInt(unit_price.getText());
            int b = Integer.parseInt(quantity.getText());
            int c = a * b;
            total.setText(String.valueOf(c));
        } else total.setText("");
    }
    @FXML
    private void print(){
        // Total spent on buy
        int total_buy = 0;

        // Initialize service and connect to printer
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
        PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
        patts.add(Sides.DUPLEX);

        PrintService[] ps = PrintServiceLookup.lookupPrintServices(flavor, patts);
        if (ps.length == 0) {
            throw new IllegalStateException("No Printer Found");
        }
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null,null);
        PrintService myService = null;
        for (PrintService p : printServices){
            //Get Printer Name
            System.out.println(p.getName());
            if(p.getName().equals("POS-58")){
                myService = p;
                break;
            }
        }
        if (myService == null){
            throw new IllegalStateException("Printer Not Found!");
        }

        // Define message
        ByteArrayOutputStream message = new ByteArrayOutputStream();

        // Add Header
        message.writeBytes("------------------------------\n".getBytes());
        message.writeBytes(String.format("%-7s %s %7s%n", "|", "HAROUNA MAIGA", "|").getBytes());
        message.writeBytes(String.format("%-6s %s %6s%n", "|", "TEL: 0103492233", "|").getBytes());
        message.writeBytes(String.format("%-6s %s %6s%n", "|", "TEL: 0170988623", "|").getBytes());
        message.writeBytes(String.format("%-7s %s %6s%n", "|", "VENTE EN GROS,", "|").getBytes());
        message.writeBytes(String.format("%-4s %s %4s%n", "|", "SEMI GROS, DETAILS ", "|").getBytes());
        message.writeBytes("------------------------------\n".getBytes());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        message.writeBytes(String.format("%-4s %s%n%n", "", dtf.format(LocalDateTime.now())).getBytes());

        // Add headboard
        String headboard = String.format("%-9s %3s %6s %7s%n%n%n", "Produit", "Qte", "Prix", "Total");
        message.writeBytes(headboard.getBytes());

        // Add the different products from list
        for (Object prod: preview.getItems()
             ) {
            if(prod.getClass().equals("".getClass())){
                String line = prod.toString().replace("\n", " ");
                String[] el = line.split(" ");
                String newline = String.format("%-9s %3d %6d %7d %n", el[0].replace("-", " "), Integer.parseInt(el[1]), Integer.parseInt(el[2]), Integer.parseInt(el[3]));
                message.writeBytes(newline.getBytes());
                total_buy += Integer.parseInt(el[3]);
            }
        }

        // Display Total
        message.writeBytes("------------------------------\n".getBytes());
        message.writeBytes(String.format("Total: %d%n", total_buy).getBytes());
        message.writeBytes("\n".getBytes());

        // Add footer
        message.writeBytes("------------------------------\n".getBytes());
        message.writeBytes("Vos Achats, chez HAROUNA!".getBytes());
        message.writeBytes("\n\n\n\n".getBytes());

        // Create Print Document
        DocPrintJob job = myService.createPrintJob();
        Doc doc = new SimpleDoc(message.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);

        // Launch Printer
        try {
            job.print(doc, new HashPrintRequestAttributeSet());
            System.out.println(message);
            preview.getItems().removeAll(preview.getItems());
        } catch (PrintException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void createProduct(){
        if (product_name.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ajoutez le nom du produit");
            alert.show();
        }
        if(unit_price.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ajoutez le prix du produit");
            alert.show();
        }
        if (!product_name.getText().equals("") && !unit_price.getText().equals("")) {
            System.out.println("Writing");
            try{
                Files.write(Paths.get(".\\src\\resources\\inventaire.txt"), (product_name.getText() + " " + unit_price.getText() + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                String cwd = Path.of("").toAbsolutePath().toString();
                System.out.println(cwd);
            }
        }
        //reload_inventory();
    }
    private void reload_inventory(){
        try {
            File inv = new File(".\\src\\resources\\inventaire.txt");
            int lines = 0;
            Scanner reader = new Scanner(inv);
            Scanner counter = new Scanner(inv);
            while (counter.hasNextLine()){
                counter.nextLine();
                lines ++;
            }
            inventory = new Product[lines];
            int curr = 0;
            while (reader.hasNextLine()){
                String data = reader.nextLine();
                String[] rel = data.split(" ");
                inventory[curr] = new Product(rel[0], Integer.parseInt(rel[1]));
                ch = new ChoiceBox<>();
                ch.getItems().add(inventory[curr].name);
                //System.out.println(ch.getItems().get(curr));
            }
        } catch (IOException e){
            String cwd = Path.of("").toAbsolutePath().toString();
            System.out.println(cwd);
        }
    }
}

class Product{
    int price;
    String name;
    public Product(String name, int price){
        this.name = name;
        this.price = price;
    }
}