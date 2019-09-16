import {style} from "./style";
import {customElement, html, LitElement, property, query} from "lit-element";

@customElement("or-header")
class OrHeader extends LitElement {

    @property({type: Boolean})
    private _drawerOpened = false;

    @property({type: String})
    private logo = "";

    @query("slot[name=mobile-bottom]")
    protected _mobileBottomSlot?: HTMLSlotElement;

    @query("div[id=mobile-bottom]")
    protected _mobileBottomDiv?: HTMLDivElement;

    static styles = style;

    protected render() {
        return html`
           <!-- Header -->
            <div id="header" class="shadow">
                <div id="toolbar-top">
                    <div><img id="logo" src="${this.logo}" /></div>
                    <!-- This gets hidden on a small screen-->
                    
                    <nav id="toolbar-list">
                        <div id="desktop-left">
                            <slot name="desktop-left"></slot>
                        </div>
                    </nav>
                
                    <div id="desktop-right">
                        <slot name="desktop-right"></slot>
                    </div>
                
                    <button id="menu-btn" title="Menu" @click="${this._menuButtonClicked}">${this._drawerOpened ? html`<or-icon icon="close"></or-icon>` : html`<or-icon icon="menu"></or-icon>`}</button>
                </div>
            </div>
            
            <div id="drawer" ?opened="${this._drawerOpened}" @click="${this._close}">
                <div>                    
                    <div id="mobile-top">
                        <nav id="drawer-list">
                            <slot name="mobile-top"></slot>
                        </nav>
                    </div>
                    
                    <div id="mobile-bottom">
                        <slot name="mobile-bottom"></slot>
                    </div>
                </div>
            </divr>
            
        `;
    }

    protected firstUpdated(_changedProperties: Map<PropertyKey, unknown>): void {
        if (this._mobileBottomSlot && this._mobileBottomDiv) {
            const childNodes = this._mobileBottomSlot.assignedNodes();
            if (childNodes.length > 0) {
                this._mobileBottomDiv.classList.add("has-children");
            }
        }
    }

    private _close() {
        this._drawerOpened = false;
    }

    private _menuButtonClicked() {
        this._drawerOpened = !this._drawerOpened;
    }
}
