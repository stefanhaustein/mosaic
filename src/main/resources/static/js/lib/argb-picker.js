class ArgbPicker extends HTMLElement {
    static get observedAttributes() {
        return ['value'];
    }

    constructor() {
        super();

        // Attach isolated Shadow DOM
        this.attachShadow({ mode: 'open' });

        // Native color input for RGB (#RRGGBB)
        this._colorInput = document.createElement('input');
        this._colorInput.type = 'color';
        this._colorInput.value = '#000000';
        this._colorInput.style.width = "40px"

        // Native slider input for Alpha (0 - 255)
        this._alphaSelect = document.createElement('select');
        this._alphaSelect.style.width = "80px"
        for (let i = 0; i <= 100; i += 10) {
            let option = document.createElement("option")
            option.setAttribute("value", option * 255 / 100);
            option.innerText = i == 0 ? "Opaque" : i == 100 ? "Transparent" : (i + "%")
            this._alphaSelect.append(option)
        }

        // Shadow DOM containing strictly the two native controls
        let container = document.createElement("div")
        container.append(this._colorInput, " ", this._alphaSelect)
        container.style.display = "flex"
        this.shadowRoot.append(container);

        // Dispatch events from host element
        const dispatch = (eventName) => {
            this.dispatchEvent(new Event(eventName, { bubbles: true, composed: true }));
        };

        this._colorInput.addEventListener('input', () => dispatch('input'));
        this._colorInput.addEventListener('change', () => dispatch('change'));
        this._alphaSelect.addEventListener('input', () => dispatch('input'));
        this._alphaSelect.addEventListener('change', () => dispatch('change'));
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === 'value' && oldValue !== newValue) {
            this.value = newValue;
        }
    }

    // Getter: returns strictly #AARRGGBB format
    get value() {
        const alphaDec = parseInt(this._alphaSelect.value, 10);
        const alphaHex = alphaDec.toString(16).padStart(2, '0').toUpperCase();
        const rgbHex = this._colorInput.value.substring(1).toUpperCase();
        return `#${alphaHex}${rgbHex}`;
    }

    // Setter: accepts strictly #AARRGGBB string
    set value(val) {
        if (typeof val !== 'string') return;

        const normalized = val.trim().toUpperCase();
        // Validate #AARRGGBB format
        if (!/^#[0-9A-FA-F]{8}$/.test(normalized)) return;

        const alphaHex = normalized.substring(1, 3);
        const rgbHex = normalized.substring(3, 9);

        this._alphaSelect.value = 10 * (parseInt(alphaHex, 16) * 10 / 255);
        this._colorInput.value = `#${rgbHex}`;
    }
}

// Register custom element
customElements.define('argb-picker', ArgbPicker);