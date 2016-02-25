/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vigica_edit.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import vigica_edit.Decompress_mw_s1;
import vigica_edit.model.Service;

/**
 *
 * @author bnabi
 */
public class FXMLMainController implements Initializable {
    
    Decompress_mw_s1 decompress = new Decompress_mw_s1();

    /**
    * The data as an observable list of Service.
    */
    private ObservableList<Service> serviceData = FXCollections.observableArrayList();
    
    @FXML
    private TableView<Service> serviceTable;
    @FXML
    private TableColumn<Service, Integer> s_idxColumn;
    @FXML
    private TableColumn<Service, String> s_nameColumn;
    @FXML
    private TableColumn<Service, String> s_typeColumn;
    @FXML
    private TableColumn<Service, Integer> s_nidColumn;
    @FXML
    private TableColumn<Service, Integer> s_pprColumn;

    @FXML
    private void handleOpenAction(ActionEvent event) {

        final String chemin = "D:\\Info\\Misc\\Vigica\\dvb_s_mw_s1";
        try {
            decompress.decompress(chemin);
            
            serviceData.addAll(decompress.getServices());
        
            serviceTable.setItems(serviceData);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCompareAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handleSaveAction(ActionEvent event) {
        
    }

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public FXMLMainController() {
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the service table with the two columns.
        s_idxColumn.setCellValueFactory(cellData -> cellData.getValue().s_idxProperty().asObject());
        s_nameColumn.setCellValueFactory(cellData -> cellData.getValue().s_nameProperty());
        s_typeColumn.setCellValueFactory(cellData -> cellData.getValue().s_typeProperty());
        s_nidColumn.setCellValueFactory(cellData -> cellData.getValue().s_nidProperty().asObject());
        s_pprColumn.setCellValueFactory(cellData -> cellData.getValue().s_pprProperty().asObject());
    }
    
}
