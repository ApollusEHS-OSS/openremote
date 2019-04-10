import {html, LitElement, customElement, property, PropertyValues} from "lit-element";
import {AttributeDescriptor, AttributeValueType, AttributeValueDescriptor, ValueType} from "@openremote/model";

import {selectStyle} from "@openremote/or-select/dist/style";

import {attributeDescriptors} from "../../const/attribute-descriptors";
import {rulesEditorConfig} from "../../const/rule-config";

@customElement("or-select-asset-attribute")
class OrSelectAssetAttribute extends LitElement {

    @property({type: String})
    public icon: string = "";

    @property({type: String})
    public assetType: string = "";

    @property({type: Array})
    public attributeDescriptors?: AttributeDescriptor[] = attributeDescriptors;

    // TODO replace with type from rulesEditor config?
    @property({type: Array})
    public options?: any[];

    @property({type: String})
    public value: any;

    @property({type: Boolean})
    public disabled: boolean = false;

    @property({type: Function})
    private changed: any;

    constructor() {
        super();
    }

    static get styles() {
        return [
            selectStyle
        ];
    }

    public onChange() {
        if (this.shadowRoot) {
            const value = (this.shadowRoot.getElementById("or-select-asset-attribute") as HTMLInputElement).value;

            const event = new CustomEvent("asset-attribute:changed", {
                detail: { value: value },
                bubbles: true,
                composed: true });

            this.dispatchEvent(event);
        }
    }

    protected render() {

        // TODO types should be based on rules-config
        return html`
             <select ?disabled="${this.disabled}" id="or-select-asset-attribute" @change="${this.onChange}">
                ${this.options ? this.options.map((attribute: AttributeDescriptor) => {
                    return html`
                        <option ?selected="${attribute.name === this.value}" value="${attribute.name}">${attribute.name}</option>
                    `;
                }) : ``}
            </select>
        `;
    }

    protected firstUpdated(_changedProperties: PropertyValues): void {
        super.firstUpdated(_changedProperties);
        if (!this.attributeDescriptors) {
            return;
        }

        this.options = this.attributeDescriptors.filter((obj) => {
            return obj.name && rulesEditorConfig.options.attributeValueDescriptors.hasOwnProperty(obj.name);
        });

    }

}
