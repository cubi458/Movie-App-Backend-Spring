class I18n {
    constructor() {
        this.currentLang = localStorage.getItem('admin_lang') || 'en';
        this.translations = {
            en: en,
            vi: vi
        };
        // Set initial active state
        document.addEventListener('DOMContentLoaded', () => {
            this.updateButtonStates();
        });
    }

    setLanguage(lang) {
        if (this.translations[lang]) {
            this.currentLang = lang;
            localStorage.setItem('admin_lang', lang);
            this.updateContent();
            this.updateButtonStates();
        }
    }

    updateButtonStates() {
        // Update active state of language buttons
        document.querySelectorAll('.lang-btn').forEach(btn => {
            const onclick = btn.getAttribute('onclick');
            if (onclick && onclick.includes(`'${this.currentLang}'`)) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
    }

    t(key) {
        return this.translations[this.currentLang][key] || key;
    }

    updateContent() {
        document.querySelectorAll('[data-i18n]').forEach(element => {
            const key = element.getAttribute('data-i18n');
            if (key) {
                if (element.tagName === 'INPUT' && element.getAttribute('type') === 'submit') {
                    element.value = this.t(key);
                } else if (element.tagName === 'INPUT' && element.getAttribute('placeholder')) {
                    element.placeholder = this.t(key);
                } else {
                    element.textContent = this.t(key);
                }
            }
        });
    }
}

// Initialize i18n
const i18n = new I18n();

// Initial translation
document.addEventListener('DOMContentLoaded', () => {
    i18n.updateContent();
}); 