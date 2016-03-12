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
package vigica_edit.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for
 * 
 * @author nabillo
 */
public class Service {
    private final StringProperty s_type;
    private final IntegerProperty s_idx;
    private final StringProperty s_name;
    private final IntegerProperty s_nid;
    private final StringProperty s_ppr;
    
    /**
    * Default constructor.
    */
    public Service() {
        this.s_type = new SimpleStringProperty("");
        this.s_idx = new SimpleIntegerProperty(0);
        this.s_name = new SimpleStringProperty("");
        this.s_nid = new SimpleIntegerProperty(0);
        this.s_ppr = new SimpleStringProperty("");
    }
    
    public Service(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s) {
        this.s_type = new SimpleStringProperty(stype);
        this.s_idx = new SimpleIntegerProperty(recd_idx);
        this.s_name = new SimpleStringProperty(rcdname_s);
        this.s_nid = new SimpleIntegerProperty(nid_d);
        this.s_ppr = new SimpleStringProperty(ppr_s);
    }

    public String getS_type() {
        return s_type.get();
    }

    public void setS_type(String s_type) {
        this.s_type.set(s_type);
    }

    public StringProperty s_typeProperty() {
        return s_type;
    }
    
    public int getS_idx() {
        return s_idx.get();
    }

    public void setS_idx(int s_idx) {
        this.s_idx.set(s_idx);
    }

    public IntegerProperty s_idxProperty() {
        return s_idx;
    }
    
    public String getS_name() {
        return s_name.get();
    }

    public void setS_name(String s_name) {
        this.s_name.set(s_name);
    }

    public StringProperty s_nameProperty() {
        return s_name;
    }
    
    public int getS_nid() {
        return s_nid.get();
    }

    public void setS_nid(int s_nid) {
        this.s_nid.set(s_nid);
    }

    public IntegerProperty s_nidProperty() {
        return s_nid;
    }
    
    public String getS_ppr() {
        return s_ppr.get();
    }

    public void setS_ppr(String s_ppr) {
        this.s_ppr.set(s_ppr);
    }

    public StringProperty s_pprProperty() {
        return s_ppr;
    }
    
}
