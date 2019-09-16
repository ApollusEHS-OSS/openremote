import {css, customElement, html, LitElement, property, PropertyValues} from 'lit-element';
import orIconset from "./or-iconset";

export interface IconSets {
    [name: string]: IconSet;
}

export interface IconSet {
    getIcon(icon: string): Element | undefined;
}

export enum IconEvent {
    ICONSET_ADDED = "or-icon-iconset-added"
}

class Icons {
    private _icons: IconSets = {
        or: orIconset
    };

    addIconSet(name: string, iconset: IconSet) {
        this._icons[name] = iconset;
        window.dispatchEvent(new CustomEvent(IconEvent.ICONSET_ADDED));
    }

    getIconSet(name: string) {
        return this._icons[name];
    }
}

export var IconSets = new Icons();

@customElement('or-icon')
export class OrIcon extends LitElement {

    static DEFAULT_ICONSET: string = "mdi";

    static styles = css`
        :host {
        
            --internal-or-icon-width: var(--or-icon-width, 24px);
            --internal-or-icon-height: var(--or-icon-height, 24px);
            --internal-or-icon-fill: var(--or-icon-fill, currentColor);
            --internal-or-icon-stroke: var(--or-icon-fill, none);
            --internal-or-icon-stroke-width: var(--or-icon-stroke-width, 0);
        
            display: inline-block;
            position: relative;
            vertical-align: middle;
            fill: var(--internal-or-icon-fill);
            stroke: var(--internal-or-icon-stroke);
            stroke-width: var(--internal-or-icon-stroke-width);
            width: var(--internal-or-icon-width);
            height: var(--internal-or-icon-height);
        }
        
        :host([hidden]) {
            display: none;
        }
    `;

    @property({type: String, reflect: true})
    icon?: string;

    protected _iconElement?: Element;
    protected _handler = (evt: Event) => {
        this._onIconsetAdded(evt);
    }

    protected render() {
        return html`
      ${this._iconElement}
    `;
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        window.removeEventListener(IconEvent.ICONSET_ADDED, this._handler);
    }

    protected shouldUpdate(changedProperties: PropertyValues): boolean {
        if (changedProperties.has("icon")) {
            this._updateIcon(false);
        }

        return true;
    }

    protected _updateIcon(requestUpdate: boolean) {
        this._iconElement = undefined;
        window.removeEventListener(IconEvent.ICONSET_ADDED, this._handler);
        let parts = (this.icon || "").split(":");
        let iconName = parts.pop();
        let iconSetName = parts.pop() || OrIcon.DEFAULT_ICONSET;
        if (!iconSetName || iconSetName === "" || !iconName || iconName === "") {
            return;
        }

        let iconSet = IconSets.getIconSet(iconSetName);

        if (!iconSet) {
            window.addEventListener(IconEvent.ICONSET_ADDED, this._handler);
            return;
        }

        this._iconElement = iconSet.getIcon(iconName);

        if (requestUpdate) {
            this.requestUpdate();
        }
    }

    protected _onIconsetAdded(evt: Event) {
        this._updateIcon(true);
    }
}

export default {
    IconSets
}