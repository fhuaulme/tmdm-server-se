package com.amalto.core.migration.tasks;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.migration.AbstractMigrationTask;
import com.amalto.core.objects.configurationinfo.ConfigurationHelper;
import com.amalto.core.objects.menu.MenuPOJO;
import com.amalto.core.server.api.Menu;
import com.amalto.core.util.Util;
import org.apache.log4j.Logger;

public class RenameMenuEntriesTask extends AbstractMigrationTask {

    private static final Logger LOGGER = Logger.getLogger(RenameMenuEntriesTask.class);

    @Override
    protected Boolean execute() {
        //Update Menu POJOS
        LOGGER.info("Updating Menus"); //$NON-NLS-1$
        try {
            String[] ids = ConfigurationHelper.getServer().getAllDocumentsUniqueID(null, "amaltoOBJECTSMenu"); //$NON-NLS-1$
            if (ids != null) {
                Menu menuCtrl = Util.getMenuCtrlLocal();
                for (String id : ids) {
                    String xml = ConfigurationHelper.getServer().getDocumentAsString(null, "amaltoOBJECTSMenu", id); //$NON-NLS-1$
                    xml = xml.replaceAll("java:com.amalto.core.ejb.MenuEntryPOJO", "java:com.amalto.core.objects.menu.ejb.MenuEntryPOJO"); //$NON-NLS-1$
                    MenuPOJO menu = ObjectPOJO.unmarshal(MenuPOJO.class, xml);
                    menuCtrl.putMenu(menu);
                    LOGGER.info("Processed '" + menu.getName() + "'");
                }
            }
        } catch (Exception e) {
            String err = "Unable to Rename Menu Entries.";
            LOGGER.error(err, e);
            return false;
        }
        LOGGER.info("Done Updating Menus");
        return true;
    }

}
