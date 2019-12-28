package pl.skotar.alfrescojvmconsole.example;

import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.apache.poi.util.StringUtil.UTF8;
import static org.springframework.http.MediaType.TEXT_PLAIN;

public class AlfrescoJvmConsoleJavaExample {

    @Autowired
    private ServiceRegistry serviceRegistry;

    public String alfresco_saveNodeAndReturnItsContent() {
        return persistNodeAndReadItsContent();
    }

    public String alfresco_saveNodeAndReturnItsContentUsingMainClassLoader() {
        // useMainClassLoader

        return persistNodeAndReadItsContent();
    }

    private String persistNodeAndReadItsContent() {
        NodeRef nodeRef = createNode(getCompanyHomeNodeRef());
        saveTextContent(nodeRef, "example");
        return readTextContent(nodeRef);
    }

    private NodeRef createNode(NodeRef nodeRef) {
        return serviceRegistry.getFileFolderService().create(
                nodeRef,
                LocalDateTime.now().toString().replace(":", "_"),
                TYPE_CONTENT
        ).getNodeRef();
    }

    private NodeRef saveTextContent(NodeRef nodeRef, String content) {
        ContentWriter contentWriter = serviceRegistry.getContentService().getWriter(nodeRef, PROP_CONTENT, true);
        contentWriter.setMimetype(TEXT_PLAIN.getType());
        contentWriter.setEncoding(UTF8.displayName());
        contentWriter.putContent(content);

        return nodeRef;
    }

    private String readTextContent(NodeRef nodeRef) {
        return serviceRegistry.getContentService().getReader(nodeRef, PROP_CONTENT)
                              .getContentString();
    }

    private NodeRef getCompanyHomeNodeRef() {
        return serviceRegistry.getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
    }
}
