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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import vigica_edit.Decompress_mw_s1;

/**
 *
 * @author bnabi
 */
public class FXMLMainController implements Initializable {
    
    Decompress_mw_s1 decompress = new Decompress_mw_s1();
    
    @FXML
    private Label label;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {

        final String chemin = "D:\\Info\\Misc\\Vigica\\dvb_s_mw_s1";
        try {
            decompress.decompress(chemin);
        }
        catch (Exception e) {
            System.out.println(e.getCause());
            label.setText(e.getMessage());
        }
       
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
