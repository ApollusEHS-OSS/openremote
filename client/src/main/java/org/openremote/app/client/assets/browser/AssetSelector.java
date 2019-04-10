package org.openremote.app.client.assets.browser;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import org.openremote.app.client.widget.*;
import org.openremote.app.client.i18n.ManagerMessages;
import java.util.function.Consumer;

/**
 * Shows a browser tree node in a form group and encapsulates the process of
 * changing it by selecting a new node in the asset browser tree.
 */
public class AssetSelector extends FormGroup {

    final AssetBrowser.Presenter assetBrowserPresenter;
    final ManagerMessages managerMessages;
    final Consumer<BrowserTreeNode> selectionConsumer;

    final FormLabel label = new FormLabel();
    final FormField field = new FormField();
    final FormGroupActions actions = new FormGroupActions();
    final FlowPanel fieldContainer = new FlowPanel();
    final FormOutputText outputTenantDisplayName = new FormOutputText();
    final FormInputText outputAssetName = new FormInputText();
    final Label infoLabel = new Label();
    final boolean enableTenantSelection;
    final boolean enableClearSelection;
    final FormButton selectAssetButton = new FormButton();
    final FormButton clearSelectionButton = new FormButton();
    final FormButton confirmButton = new FormButton();

    // The node that was selected before the selection process started
    protected BrowserTreeNode originalNode;

    // The node that is currently selected
    protected BrowserTreeNode selectedNode;

    public AssetSelector(AssetBrowser.Presenter assetBrowserPresenter,
                         ManagerMessages managerMessages,
                         String labelText,
                         String infoText,
                         boolean enableClearSelection,
                         Consumer<BrowserTreeNode> selectionConsumer) {
        this(assetBrowserPresenter, managerMessages, labelText, infoText, true, enableClearSelection, selectionConsumer);
    }

    public AssetSelector(AssetBrowser.Presenter assetBrowserPresenter,
                         ManagerMessages managerMessages,
                         String labelText,
                         String infoText,
                         boolean enableTenantSelection,
                         boolean enableClearSelection,
                         Consumer<BrowserTreeNode> selectionConsumer) {
        this.assetBrowserPresenter = assetBrowserPresenter;
        this.managerMessages = managerMessages;
        this.selectionConsumer = selectionConsumer;
        this.enableTenantSelection = enableTenantSelection;
        this.enableClearSelection = enableClearSelection;
        setFormLabel(label);
        setFormField(field);
        setFormGroupActions(actions);
        addInfolabel(infoLabel);
        field.add(fieldContainer);
        fieldContainer.setStyleName("flex layout vertical");
        fieldContainer.add(outputTenantDisplayName);
        fieldContainer.add(outputAssetName);
        actions.add(selectAssetButton);
        actions.add(clearSelectionButton);
        actions.add(confirmButton);

        setAlignStart(true);

        formLabel.setText(labelText);
        infoLabel.setText(infoText);

        outputAssetName.setReadOnly(true);
        outputAssetName.addStyleName("flex");

        selectAssetButton.setIcon("external-link-square");
        selectAssetButton.setText(managerMessages.selectAsset());
        selectAssetButton.addClickHandler(event -> beginSelection());

        clearSelectionButton.setIcon("remove");
        clearSelectionButton.setText(managerMessages.clearSelection());
        clearSelectionButton.addClickHandler(event -> clearSelection());

        confirmButton.setIcon("check");
        confirmButton.setText(managerMessages.OK());
        confirmButton.addClickHandler(event -> endSelection());

        init();
    }

    public void init() {
        originalNode = null;
        selectedNode = null;
        outputTenantDisplayName.setText(null);
        outputTenantDisplayName.setVisible(false);
        outputAssetName.setVisible(true);
        outputAssetName.setText(managerMessages.noAssetSelected());
        infoLabel.setVisible(false);
        selectAssetButton.setVisible(true);
        clearSelectionButton.setVisible(false);
        confirmButton.setVisible(false);
        assetBrowserPresenter.useSelector(null);
    }

    public void beginSelection() {
        selectAssetButton.setVisible(false);
        clearSelectionButton.setVisible(enableClearSelection);
        confirmButton.setVisible(true);
        infoLabel.setVisible(true);

        assetBrowserPresenter.useSelector(this);
        originalNode = assetBrowserPresenter.getSelectedNode();
        assetBrowserPresenter.clearSelection();
    }

    public void clearSelection() {
        setSelectedNode(null);
    }

    public void setSelectedNode(BrowserTreeNode selectedNode) {
        if (!enableTenantSelection && (selectedNode instanceof TenantTreeNode)) {
            return;
        }
        this.selectedNode = selectedNode;
        renderTreeNode(managerMessages, selectedNode, outputTenantDisplayName, outputAssetName);
    }

    public void endSelection() {
        selectAssetButton.setVisible(true);
        clearSelectionButton.setVisible(false);
        confirmButton.setVisible(false);
        infoLabel.setVisible(false);

        assetBrowserPresenter.useSelector(null);
        if (originalNode != null) {
            if (originalNode instanceof TenantTreeNode) {
                assetBrowserPresenter.selectRealm(originalNode.getId());
            } else if (originalNode instanceof AssetTreeNode) {
                assetBrowserPresenter.selectAssetById(originalNode.getId());
            }
            originalNode = null;
        } else {
            assetBrowserPresenter.clearSelection();
        }

        selectionConsumer.accept(selectedNode);
    }

    public void setEnabled(boolean enabled) {
        selectAssetButton.setEnabled(enabled);
    }

    public static void renderTreeNode(ManagerMessages managerMessages,
                                      BrowserTreeNode treeNode,
                                      FormOutputText outputTenantDisplayName,
                                      FormInputText outputAssetName) {
        if (treeNode == null) {
            outputTenantDisplayName.setVisible(false);
            outputAssetName.setVisible(true);
            outputAssetName.setText(managerMessages.noAssetSelected());
        } else if (treeNode instanceof TenantTreeNode) {
            TenantTreeNode tenantTreeNode = (TenantTreeNode) treeNode;
            outputTenantDisplayName.setVisible(true);
            outputTenantDisplayName.setText(tenantTreeNode.getLabel());
            outputAssetName.setVisible(false);
            outputAssetName.setText(null);
        } else if (treeNode instanceof AssetTreeNode) {
            AssetTreeNode assetTreeNode = (AssetTreeNode) treeNode;
            outputTenantDisplayName.setVisible(true);
            outputTenantDisplayName.setText(assetTreeNode.getAsset().getRealm());
            outputAssetName.setVisible(true);
            outputAssetName.setText(treeNode.getLabel());
        }
    }
}
