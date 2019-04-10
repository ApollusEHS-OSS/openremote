import {html, LitElement, property, customElement} from 'lit-element';
import {AttributeValueType, BaseAssetQueryMatch, BaseAssetQueryOperator} from "@openremote/model";

import {selectStyle} from "@openremote/or-select/dist/style";

@customElement('or-select-operator')
class OrRuleWhen extends LitElement {

    @property({type: String})
    type?: AttributeValueType;

    @property({type: String})
    value?: BaseAssetQueryOperator;

    @property({type: Boolean})
    disabled: boolean = false;

    static get styles() {
        return [
            selectStyle
        ];
    }

    protected render() {

        return html`
             ${this.type ? html`
                <select ?disabled="${this.disabled}" id="or-select-operator" @change="${this.onChange}">
                    <option ?selected="${this.value === BaseAssetQueryOperator.EQUALS}" value="${BaseAssetQueryOperator.EQUALS}">=</option>
                    <option ?selected="${this.value === BaseAssetQueryOperator.NOT_EQUALS}" value="${BaseAssetQueryOperator.NOT_EQUALS}">!=</option>
                    ${this.type === AttributeValueType.NUMBER ? html`
                        <option ?selected="${this.value === BaseAssetQueryOperator.LESS_THAN}" value="${BaseAssetQueryOperator.LESS_THAN}" value="LESS_THAN"><</option>
                        <option ?selected="${this.value === BaseAssetQueryOperator.GREATER_THAN}" value="${BaseAssetQueryOperator.GREATER_THAN}" value="GREATER_THAN">></option>
                        <option ?selected="${this.value === BaseAssetQueryOperator.LESS_EQUALS}" value="${BaseAssetQueryOperator.LESS_EQUALS}" value="LESS_EQUALS">=<</option>
                        <option ?selected="${this.value === BaseAssetQueryOperator.GREATER_EQUALS}" value="${BaseAssetQueryOperator.GREATER_EQUALS}" value="GREATER_EQUALS">=></option>
                    ` :``}
                </select>
            ` :``}
        `;
    }


    onChange() {
        if(this.shadowRoot){
            const value = (<HTMLInputElement>this.shadowRoot.getElementById('or-select-operator')).value;

            let event = new CustomEvent('operator:changed', {
                detail: { value: value },
                bubbles: true,
                composed: true });

            this.dispatchEvent(event);
        }
    }

    constructor() {
        super();
    }


}

