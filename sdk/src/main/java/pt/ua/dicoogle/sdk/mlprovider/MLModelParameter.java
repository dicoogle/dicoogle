/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle-sdk.
 *
 * Dicoogle/dicoogle-sdk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle-sdk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.sdk.mlprovider;

import java.util.List;

/**
 * This object is used to map MLModel parameters.
 * These parameters are used to fine tune a inference request to a model.
 */
public class MLModelParameter {

    private MLModelParameterType type;
    private String name;
    private Object defaultValue;
    private String description;
    private List<Choice> choices;

    public enum MLModelParameterType{
        TEXT,
        NUMBER,
        ENUM
    }

    public static class Choice {

        private String name;
        private Object value;

        public Choice(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private MLModelParameter(MLModelParameterType type, String name, Object defaultValue, String description) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    private MLModelParameter(String name, List<Choice> choices, Object defaultValue, String description){
        this(MLModelParameterType.ENUM, name, defaultValue, description);
        this.choices = choices;
    }

    public static MLModelParameter buildNumberParam(String name, double defaultValue, String description){
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException();
        return new MLModelParameter(MLModelParameterType.NUMBER, name, defaultValue, description);
    }

    public static MLModelParameter buildTextParam(String name, String defaultValue, String description){
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException();
        return new MLModelParameter(MLModelParameterType.TEXT, name, defaultValue, description);
    }

    public static MLModelParameter buildEnumParam(String name, List<Choice> choices, Object defaultValue, String description){
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException();
        if(choices == null || choices.isEmpty())
            throw new IllegalArgumentException();
        return new MLModelParameter(name, choices, defaultValue, description);
    }

    public MLModelParameterType getType() {
        return type;
    }

    public void setType(MLModelParameterType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}
