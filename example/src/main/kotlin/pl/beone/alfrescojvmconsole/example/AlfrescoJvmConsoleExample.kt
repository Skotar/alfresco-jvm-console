package pl.beone.alfrescojvmconsole.example

import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.model.ContentModel.TYPE_CONTENT
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.NodeRef
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.TEXT_PLAIN
import java.time.LocalDateTime
import kotlin.text.Charsets.UTF_8

class AlfrescoJvmConsoleExample {

    @Autowired
    private lateinit var serviceRegistry: ServiceRegistry

    fun `alfresco _ save node and return its content`(): String =
        persistNodeAndReadItsContent()

    fun `alfresco _ save node and return its content using main class loader`(): String {
        // useMainClassLoader

        return persistNodeAndReadItsContent()
    }

    private fun persistNodeAndReadItsContent(): String =
        getCompanyHomeNodeRef()
            .createNode()
            .saveTextContent("example")
            .readTextContent()

    private fun NodeRef.createNode(): NodeRef =
        serviceRegistry.fileFolderService.create(
            this,
            LocalDateTime.now().toString().replace(":", "_"),
            TYPE_CONTENT
        ).nodeRef

    private fun NodeRef.saveTextContent(content: String): NodeRef {
        serviceRegistry.contentService.getWriter(this, PROP_CONTENT, true).apply {
            mimetype = TEXT_PLAIN.type
            encoding = UTF_8.name()
        }.also { it.putContent(content) }

        return this
    }

    private fun NodeRef.readTextContent(): String =
        serviceRegistry.contentService.getReader(this, PROP_CONTENT)
            .contentString

    private fun getCompanyHomeNodeRef(): NodeRef =
        serviceRegistry.nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null)
}
