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
package pt.ua.dicoogle.sdk.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

/**
 * Structure to manage all the tags inside Dicoogle.
 * 
 * There are three groups. DIM Fields, DICOM Fields and PrivateFields.
 * DIM Fields may either be DICOM or Private.
 * DICOM and Private groups do not overlap
 * 
 * An "Other" field is the common designation for fields that do not belong to the DIM.
 *
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Tiago Marques Godinho <tmgodinho@ua.pt> Refactor
 */
public class TagsStruct {
    private static final Logger logger = LoggerFactory.getLogger(TagsStruct.class);

    // Optimization @TODO
    // Map Structures
    private BidiMap<Integer, String> tagNameMappings;
    private HashMap<Integer, TagValue> tagValueMappings;
    // Lists (DIM, DICOM, PrivateFields)
    private Set<TagValue> nDIMFields;
    private Set<TagValue> nDICOMFields;
    private Set<TagValue> nPrivateFields;
    // Modalities
    private Set<String> modalitiesSet;
    // Index All Modalities
    private boolean indexAllModalities = false;
    // DeepSearch modalities
    private boolean deepSearchModalities = true;

    private List<String> dictionaries = new ArrayList<>();

    private static TagsStruct instance = null;

    /** Obtain a global instance of Tag information.
     *
     * @return the application's global instance, for use in production
     */
    public static synchronized TagsStruct getInstance() {
        if (instance == null) {
            instance = new TagsStruct();
        }
        return instance;
    }


    private TagsStruct() {
        // Initialize fields;
        this.tagNameMappings = new DualHashBidiMap<>();
        this.tagValueMappings = new HashMap<>();
        this.nDICOMFields = new HashSet<>();
        this.nDIMFields = new HashSet<>();
        this.nPrivateFields = new HashSet<>();
        this.modalitiesSet = new HashSet<>();

        Map<String, Integer> tmp = DictionaryAccess.getInstance().getTagList();
        for (Entry<String, Integer> e : tmp.entrySet()) {
            addDICOMField(new TagValue(e.getValue(), e.getKey()));
        }

        // add FMI fields not yet recognized by this version of dcm4che
        final int SOURCE_APPLICATION_ENTITY_TITLE = 0x0002_0016;
        final int SENDING_APPLICATION_ENTITY_TITLE = 0x0002_0017;
        final int RECEIVING_APPLICATION_ENTITY_TITLE = 0x0002_0018;
        final int SOURCE_PRESENTATION_ADDRESS = 0x0002_0026;
        final int SENDING_PRESENTATION_ADDRESS = 0x0002_0027;
        final int RECEIVING_PRESENTATION_ADDRESS = 0x0002_0028;
        addDICOMField(new TagValue(SOURCE_APPLICATION_ENTITY_TITLE, "SourceApplicationEntityTitle"));
        addDICOMField(new TagValue(SENDING_APPLICATION_ENTITY_TITLE, "SendingApplicationEntityTitle"));
        addDICOMField(new TagValue(RECEIVING_APPLICATION_ENTITY_TITLE, "ReceivingApplicationEntityTitle"));
        addDICOMField(new TagValue(SOURCE_PRESENTATION_ADDRESS, "SourcePresentationAddress"));
        addDICOMField(new TagValue(SENDING_PRESENTATION_ADDRESS, "SendingPresentationAddress"));
        addDICOMField(new TagValue(RECEIVING_PRESENTATION_ADDRESS, "ReceivingPresentationAddress"));

        addDIMField(new TagValue(Integer.parseInt("1021c0", 16), "PregnancyStatus"));
        addDIMField(new TagValue(Integer.parseInt("81050", 16), "PerformingPhysicianName"));
        addDIMField(new TagValue(Integer.parseInt("400243", 16), "PerformedLocation"));
        addDIMField(new TagValue(Integer.parseInt("100020", 16), "PatientID"));
        addDIMField(new TagValue(Integer.parseInt("80080", 16), "InstitutionName"));
        addDIMField(new TagValue(Integer.parseInt("80050", 16), "AccessionNumber"));
        addDIMField(new TagValue(Integer.parseInt("80020", 16), "StudyDate"));
        addDIMField(new TagValue(Integer.parseInt("102154", 16), "PatientTelephoneNumbers"));
        addDIMField(new TagValue(Integer.parseInt("101010", 16), "PatientAge"));
        addDIMField(new TagValue(Integer.parseInt("81070", 16), "OperatorName"));
        addDIMField(new TagValue(Integer.parseInt("200010", 16), "StudyID"));
        addDIMField(new TagValue(Integer.parseInt("81040", 16), "InstitutionDepartmentName"));
        addDIMField(new TagValue(Integer.parseInt("20000e", 16), "SeriesInstanceUID"));
        addDIMField(new TagValue(Integer.parseInt("20000d", 16), "StudyInstanceUID"));
        addDIMField(new TagValue(Integer.parseInt("100040", 16), "PatientSex"));
        addDIMField(new TagValue(Integer.parseInt("201208", 16), "NumberOfStudyRelatedInstances"));
        addDIMField(new TagValue(Integer.parseInt("100010", 16), "PatientName"));
        addDIMField(new TagValue(Integer.parseInt("80070", 16), "Manufacturer"));
        addDIMField(new TagValue(Integer.parseInt("81090", 16), "ManufacturerModelName"));
        addDIMField(new TagValue(Integer.parseInt("81030", 16), "StudyDescription"));
        addDIMField(new TagValue(Integer.parseInt("700", 16), "Priority"));
        addDIMField(new TagValue(Integer.parseInt("80090", 16), "ReferringPhysicianName"));
        addDIMField(new TagValue(Integer.parseInt("80061", 16), "ModalitiesInStudy"));
        addDIMField(new TagValue(Integer.parseInt("80060", 16), "Modality"));
        addDIMField(new TagValue(Integer.parseInt("80030", 16), "StudyTime"));
        addDIMField(new TagValue(Integer.parseInt("80018", 16), "SOPInstanceUID"));
        addDIMField(new TagValue(Integer.parseInt("80050", 16), "AccessionNumber"));
        addDIMField(new TagValue(Integer.parseInt("80070", 16), "Manufacturer"));
        addDIMField(new TagValue(Integer.parseInt("700", 16), "Priority"));
        addDIMField(new TagValue(Integer.parseInt("200011", 16), "SeriesNumber"));
        addDIMField(new TagValue(Integer.parseInt("08103e", 16), "SeriesDescription"));
        addDIMField(new TagValue(Integer.parseInt("100030", 16), "PatientBirthDate"));

        addModality("XA");
        addModality("CT");
        addModality("US");
        addModality("MG");
        addModality("MR");
    }

    /**
     * Adds a new Tag to the structure.
     * 
     * @param field
     */
    private synchronized void addTag(TagValue field) {
        TagValue tmp = this.tagValueMappings.get(field.getTagNumber());
        if (tmp == null) {
            this.tagNameMappings.put(field.getTagNumber(), field.getName());
            this.tagValueMappings.put(field.getTagNumber(), field);
        } else {
            // merge tag information
            tmp.updateTagInformation(field);
            this.tagNameMappings.put(tmp.getTagNumber(), tmp.getName());
        }
    }

    /**
     * Removes a given Tag from the structure
     * @param field
     */
    private synchronized void removeTag(TagValue field) {
        this.tagNameMappings.remove(field.getTagNumber());
        this.tagValueMappings.remove(field.getTagNumber());
    }

    /**
     * Adds a DIM Field. If the field already exists its information is updated.
     * @param field
     */
    public synchronized void addDIMField(TagValue field) {
        // this.dimFields.put(field.getTagNumber(), field);
        this.nDIMFields.add(field);
        if (!isDICOMField(field) && !isPrivateField(field))
            addPrivateField(field);
        addTag(field);
    }

    /**
     * Adds a private field. Checks if the field is already present in the DICOM fields.
     * @param field
     * @return
     */
    public synchronized boolean addPrivateField(TagValue field) {
        addTag(field);
        if (this.nDICOMFields.contains(field)) {
            return true;
        }
        this.nPrivateFields.add(field);
        return true;
    }

    /**
     * Removes a Private Field. It is not possible to remove the field if it belongs to the DIM or to the DICOM group.
     * @param tagNumber
     * @return
     */
    public synchronized boolean removePrivateField(int tagNumber) {
        // this.dimFields.put(field.getTagNumber(), field);
        TagValue tag = this.tagValueMappings.get(tagNumber);
        if (this.nDICOMFields.contains(tag)) {
            return false;
        }

        this.nPrivateFields.remove(tag);
        this.nDIMFields.remove(tag);
        removeTag(tag);
        return true;
    }

    /**
     * Adds a DICOM Field.
     * @param field
     */
    private synchronized void addDICOMField(TagValue field) {
        // this.dimFields.put(field.getTagNumber(), field);
        this.nDICOMFields.add(field);
        addTag(field);
    }

    /**
     * Adds a new Modality.
     * @param modalityName
     */
    public void addModality(String modalityName) {
        this.modalitiesSet.add(modalityName);
    }

    /**
     * Removes All modalities.
     */
    public void removeAllModalities() {
        this.modalitiesSet = new HashSet<>();
    }

    /**
     * Removes the specified modality.
     * @param modality
     * @return true if the modality was present in the structure.
     */
    public boolean removeModality(String modality) {
        return this.modalitiesSet.remove(modality);
    }

    /**
     * Gets a read-only view of the DIM Fields
     * @return
     */
    public Set<TagValue> getDIMFields() {
        return SetUtils.unmodifiableSet(this.nDIMFields);
    }

    /**
     * Gets a List with the DIM fields names.
     * @return
     */
    public ArrayList<String> getDIMTagNames() {
        ArrayList<String> tagNames = new ArrayList<>(this.nDIMFields.size());
        for (TagValue tag : this.nDIMFields) {
            tagNames.add(tag.getName());
        }
        return tagNames;
    }

    /**
     * Gets a List with the DIM fields alias.
     * @return
     */
    public ArrayList<String> getDIMAlias() {
        ArrayList<String> alias = new ArrayList<>(this.nDIMFields.size());
        for (TagValue tag : this.nDIMFields) {
            alias.add(tag.getAlias());
        }
        return alias;
    }

    /**
     * @return a Read-only view of all fields.
     */
    public Set<TagValue> getAllFields() {
        return SetUtils.unmodifiableSet(new HashSet<>(tagValueMappings.values()));
    }

    /**
     * @return a Read only view of the Private Fields
     */
    public Set<TagValue> getPrivateFields() {
        return SetUtils.unmodifiableSet(nPrivateFields);
    }

    /**
     * @return the Fields which do not belong to the DIM. (DICOM+Private)
     */
    public Set<TagValue> getOtherFields() {

        HashSet<TagValue> tags = new HashSet<>(this.nDICOMFields.size() + this.nPrivateFields.size());
        for (TagValue tag : this.tagValueMappings.values()) {
            if (!isDICOMField(tag))
                tags.add(tag);

        }
        return tags;
    }

    /**
     * 
     * @param tagNumber
     * @return The TagValue object for the given Number, or null if there is no such tag.
     */
    public TagValue getTagValue(int tagNumber) {
        return this.tagValueMappings.get(tagNumber);
    }

    /**
     * 
     * @param tagName
     * @return The TagValue object for the given name, or null if there is no such tag.
     */
    public TagValue getTagValue(String tagName) {
        Integer x = this.tagNameMappings.getKey(tagName);
        if (x == null)
            return null;
        return this.getTagValue(x);
    }

    /**
     * @param tag
     * @return Whether the given tag is a DIM Field or not.
     */
    public boolean isDIMField(TagValue tag) {
        return this.nDIMFields.contains(tag);
    }

    /**
     * @param tag
     * @return Whether the given tag is a DICOM Field or not.
     */
    public boolean isDICOMField(TagValue tag) {
        return this.nDICOMFields.contains(tag);
    }

    /**
     * @param tag
     * @return Whether the given tag is a Private Field or not.
     */
    public boolean isPrivateField(TagValue tag) {
        return this.nPrivateFields.contains(tag);
    }

    /**
     * @param tag
     * @return Whether the given tag is an other field or not. (OtherFields = PrivateFields + DICOMFields)
     */
    public boolean isOtherField(TagValue tag) {
        return containsTag(tag.getTagNumber()) && !isDICOMField(tag);
    }

    /**
     * Checks if the tag is present in the structure.
     * @param tag 
     * @return 
     */
    public boolean containsTag(int tag) {
        return this.tagValueMappings.containsKey(tag);
    }

    /**
     * Checks if the given modality is present in the structure.
     * @param modality
     * @return
     */
    public boolean containsModality(String modality) {
        return this.modalitiesSet.contains(modality);
    }

    /**
     * @return a Read-only view of the Modalities.
     */
    public Set<String> getModalities() {
        return SetUtils.unmodifiableSet(modalitiesSet);
    }

    /**
     * Checks if the given modality is Enabled.
     * @param modality
     * @return
     */
    public boolean isModalityEnable(String modality) {
        return isIndexAllModalitiesEnabled() || this.containsModality(modality);
    }

    /**
     * @return the indexAllModalities
     */
    public boolean isIndexAllModalitiesEnabled() {
        return indexAllModalities;
    }

    /**
     * @param indexAllModalities the indexAllModalities to set
     */
    public void enableIndexAllModalities(boolean indexAllModalities) {
        this.indexAllModalities = indexAllModalities;
    }

    public boolean isDeepSearchModalitiesEnabled() {
        return deepSearchModalities;
    }


    public void enableDeepSearchModalities(boolean deepSearchModalities) {
        this.deepSearchModalities = deepSearchModalities;
    }


    /**
     * @return the dictionaries
     */
    public List<String> getDictionaries() {
        return dictionaries;
    }

    /**
     * @param dictionaries the dictionaries to set
     */
    public void setDictionaries(List<String> dictionaries) {
        this.dictionaries = dictionaries;
    }


    public void addDicionary(String dic) {
        this.dictionaries.add(dic);
    }

    public void removeDicionary(String dic) {
        this.dictionaries.remove(dic);
    }


}
