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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A label object that belongs to a model.
 * Labels must be unique within a model.
 * The label definition proposed here follows the DICOM standard guidelines for segmentation objects.
 * @see C.8.20.2 Segmentation Image Module for more information.
 */
public class MLlabel implements Comparable<MLlabel>, Serializable {

    public enum CodingSchemeDesignator {
        DCM, // DICOM scheme code designator
        SRT, // SNOMED-RT scheme code designator
        SCT, // SNOMED
        LN // LOINC scheme code designator
    }

    /**
     * DICOM Segment Label (0062, 0005) is a user defined label.
     */
    private String name;

    /**
     * DICOM Segment Description (0062, 0007) is a user defined description.
     */
    private String description;

    /**
     * A rgba color that specifies the color and opacity this label should have.
     */
    private int[] color;

    /**
     * DICOM Code Value (0008,0100) is an identifier that is unambiguous within the Coding Scheme denoted by Coding Scheme Designator (0008,0102) and Coding Scheme Version (0008,0103).
     * This is used in SegmentedPropertyTypeCodeSequence (0062,000F).
     */
    private String typeCodeValue;

    /**
     * DICOM Code Meaning (0008,0104), a human-readable description of the label, <br>
     * given by the combination of Code Value and Coding Scheme Designator.
     * This is used in SegmentedPropertyTypeCodeSequence (0062,000F).
     */
    private String typeCodeMeaning;

    /**
     * DICOM attribute Coding Scheme Designator (0008,0102) defines the coding scheme in which the code for a term is defined.
     * Typical values: "DCM" for DICOM defined codes, "SRT" for SNOMED and "LN" for LOINC
     * This is used in SegmentedPropertyTypeCodeSequence (0062,000F).
     */
    private CodingSchemeDesignator typeCodingSchemeDesignator;

    /**
     * DICOM Code Value (0008,0100) is an identifier that is unambiguous within the Coding Scheme denoted by Coding Scheme Designator (0008,0102) and Coding Scheme Version (0008,0103).
     * This is used in SegmentedPropertyCategoryCodeSequence (0062,000F).
     */
    private String categoryCodeValue;

    /**
     * DICOM Code Meaning (0008,0104), a human-readable description of the label, <br>
     * given by the combination of Code Value and Coding Scheme Designator.
     * This is used in SegmentedPropertyCategoryCodeSequence (0062,000F).
     */
    private String categoryCodeMeaning;

    /**
     * DICOM attribute Coding Scheme Designator (0008,0102) defines the coding scheme in which the code for a term is defined.
     * Typical values: "DCM" for DICOM defined codes, "SRT" for SNOMED and "LN" for LOINC
     * This is used in SegmentedPropertyCategoryCodeSequence (0062,000F).
     */
    private CodingSchemeDesignator categoryCodingSchemeDesignator;

    /**
     * Generic meta information that might be appended to this label
     */
    private Map<String, Object> meta;

    public MLlabel() {
        this.description = "unknown";
        this.typeCodingSchemeDesignator = CodingSchemeDesignator.DCM;
        this.typeCodeValue = "333333";
        this.typeCodeMeaning = "unknown";

        this.categoryCodingSchemeDesignator = CodingSchemeDesignator.DCM;
        this.categoryCodeValue = "333333";
        this.categoryCodeMeaning = "unknown";

        this.color = new int[]{0, 0, 0, 0};
        this.meta = new HashMap<>();
    }

    public MLlabel(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int[] getColor() {
        return color;
    }

    public void setColor(int[] color) {
        this.color = color;
    }

    public String getTypeCodeValue() {
        return typeCodeValue;
    }

    public void setTypeCodeValue(String typeCodeValue) {
        this.typeCodeValue = typeCodeValue;
    }

    public String getTypeCodeMeaning() {
        return typeCodeMeaning;
    }

    public void setTypeCodeMeaning(String typeCodeMeaning) {
        this.typeCodeMeaning = typeCodeMeaning;
    }

    public CodingSchemeDesignator getTypeCodingSchemeDesignator() {
        return typeCodingSchemeDesignator;
    }

    public void setTypeCodingSchemeDesignator(CodingSchemeDesignator typeCodingSchemeDesignator) {
        this.typeCodingSchemeDesignator = typeCodingSchemeDesignator;
    }

    public String getCategoryCodeValue() {
        return categoryCodeValue;
    }

    public void setCategoryCodeValue(String categoryCodeValue) {
        this.categoryCodeValue = categoryCodeValue;
    }

    public String getCategoryCodeMeaning() {
        return categoryCodeMeaning;
    }

    public void setCategoryCodeMeaning(String categoryCodeMeaning) {
        this.categoryCodeMeaning = categoryCodeMeaning;
    }

    public CodingSchemeDesignator getCategoryCodingSchemeDesignator() {
        return categoryCodingSchemeDesignator;
    }

    public void setCategoryCodingSchemeDesignator(CodingSchemeDesignator categoryCodingSchemeDesignator) {
        this.categoryCodingSchemeDesignator = categoryCodingSchemeDesignator;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MLlabel mLlabel = (MLlabel) o;
        return name.equals(mLlabel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(MLlabel o) {
        return o.getName().compareTo(this.getName());
    }
}
