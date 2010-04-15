/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.metadata.dom.saml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A pipeline stage that will remove SAML EntityDescriptior elements which do meet specified filtering criteria.
 */
public class SAMLEntityFilterStage extends AbstractComponent implements Stage<DomMetadataElement> {

    // TODO remove binding, services, white/blacklist extensions

    /** Whether EntityDescriptor metadata elements that do not contain roles, after filtering, should be removed. */
    private boolean removeRolelessEntities;

    /** Roles which should be retained within an entity descriptor. */
    private Collection<QName> whitelistedRoles;

    /** Indicates that Organization elements should be removed from EntityDescriptors. */
    private boolean removeOrganization;

    /** Indicates that ContactPerson elements should be removed from EntityDescriptors. */
    private boolean removeContactPerson;

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public SAMLEntityFilterStage(String stageId) {
        super(stageId);

        removeRolelessEntities = true;
        whitelistedRoles = new ArrayList<QName>();
        removeOrganization = true;
        removeContactPerson = true;
    }
    
    public boolean getRemoveRolelessEntities(){
        return removeRolelessEntities;
    }
    
    public void setRemoveRolelessEntities(boolean remove){
        removeRolelessEntities = remove;
    }
    
    public Collection<QName> getWhitelistedRoles(){
        return whitelistedRoles;
    }
    
    public void setWhitelistedRoles(Collection<QName> roles){
        whitelistedRoles = roles;
    }
    
    public boolean getRemoveOrganizations(){
        return removeOrganization;
    }
    
    public void setRemoveOrganization(boolean remove){
        removeOrganization = remove;
    }
    
    public boolean getRemoveContactPerson(){
        return removeContactPerson;
    }
    
    public void setRemoveContactPerson(boolean remove){
        removeContactPerson = remove;
    }

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters,
            MetadataElementCollection<DomMetadataElement> metadata) {

        ArrayList<DomMetadataElement> markedForRemoval = new ArrayList<DomMetadataElement>();
        Element descriptor;
        for (DomMetadataElement metadataElement : metadata) {
            descriptor = metadataElement.getEntityMetadata();

            NodeList children = descriptor.getChildNodes();
            Element child;
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    child = (Element) children.item(i);
                    removeRoleDescriptor(descriptor, child);
                    removeOrganization(descriptor, child);
                    removeContactPerson(descriptor, child);
                }
            }

            if (shouldRemoveEntityDescriptor(descriptor)) {
                markedForRemoval.add(metadataElement);
            }
        }
        
        metadata.removeAll(markedForRemoval);

        return metadata;
    }

    /**
     * Removes roles from the EntityDescriptor that are not whitelisted, if there are whitelisted roles.
     * 
     * @param entityDescriptor the entity descriptor from which the role elements are to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void removeRoleDescriptor(Element entityDescriptor, Element childElement) {
        if (whitelistedRoles.isEmpty()) {
            return;
        }

        if (!isRole(childElement)) {
            return;
        }

        boolean removeRole = false;
        if (SAMLConstants.MD_NS.equals(childElement.getNamespaceURI())) {
            if ("RoleDescriptor".equals(childElement.getLocalName())) {
                // TODO
            } else {
                QName roleQName = new QName(SAMLConstants.MD_NS, childElement.getLocalName());
                if (!whitelistedRoles.contains(roleQName)) {
                    removeRole = true;
                }
            }
        }

        if (removeRole) {
            entityDescriptor.removeChild(childElement);
        }
    }

    /**
     * Checks whether the given element is a role descriptor.
     * 
     * @param element the element to be checked
     * 
     * @return true if the element is a role descriptor, false otherwise
     */
    protected boolean isRole(Element element) {
        if (SAMLConstants.MD_NS.equals(element.getNamespaceURI())) {
            String localName = element.getLocalName();
            if ("RoleDescriptor".equals(localName) || "IDPSSODescriptor".equals(localName)
                    || "SPSSODescriptor".equals(localName) || "AuthnAuthorityDescriptor".equals(localName)
                    || "AttributeAuthorityDescriptor".equals(localName) || "PDPDescriptor".equals(localName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes the Organization element, if {@link #removeOrganization} is true, from an EntityDescriptor element.
     * 
     * @param entityDescriptor the entity descriptor from which the organization element is to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void removeOrganization(Element entityDescriptor, Element childElement) {
        if (!removeOrganization) {
            return;
        }

        if ("Organization".equals(childElement.getLocalName())
                && SAMLConstants.MD_NS.equals(childElement.getNamespaceURI())) {
            entityDescriptor.removeChild(childElement);
        }
    }

    /**
     * Removes the ContactPerson element, if {@link #removeContactPerson} is true, from an EntityDescriptor element.
     * 
     * @param entityDescriptor the entity descriptor from which the contact person element is to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void removeContactPerson(Element entityDescriptor, Element childElement) {
        if (!removeContactPerson) {
            return;
        }

        if ("ContactPerson".equals(childElement.getLocalName())
                && SAMLConstants.MD_NS.equals(childElement.getNamespaceURI())) {
            entityDescriptor.removeChild(childElement);
        }
    }

    /**
     * Checks whether the given EntityDescriptor should be removed. A descriptor should be removed if
     * {@link #removeRolelessEntities} is true and the descriptor contains no roles.
     * 
     * @param entityDescriptor entity descriptor to be checked
     * 
     * @return true if the entityDescriptor should be removed, false otherwise
     */
    protected boolean shouldRemoveEntityDescriptor(Element entityDescriptor) {
        if (!removeRolelessEntities) {
            return false;
        }

        NodeList children = entityDescriptor.getChildNodes();
        Element child;
        String localName;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                child = (Element) children.item(i);
                if (SAMLConstants.MD_NS.equals(child.getNamespaceURI())) {
                    localName = child.getLocalName();
                    if ("RoleDescriptor".equals(localName) || "IDPSSODescriptor".equals(localName)
                            || "SPSSODescriptor".equals(localName) || "AuthnAuthorityDescriptor".equals(localName)
                            || "AttributeAuthorityDescriptor".equals(localName) || "PDPDescriptor".equals(localName)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}