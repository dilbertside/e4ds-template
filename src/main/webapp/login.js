Ext.onReady(function() {
	Ext.QuickTips.init();

	var login = Ext.create('Ext.form.Panel', {
		frame: true,
		title: i18n.login_title,
		url: 'login.html',
		width: 320,
		iconCls: 'icon-login',

		standardSubmit: true,

		defaults: {
			anchor: '100%'
		},

		defaultType: 'textfield',

		fieldDefaults: {
			msgTarget: 'side'
		},

		items: [ {
			fieldLabel: i18n.user_username,
			name: 'username',
			allowBlank: false,
			listeners: {
				specialkey: function(field, e) {
					if (e.getKey() == e.ENTER) {
						submitForm();
					}
				}
			}
		}, {
			fieldLabel: i18n.user_password,
			name: 'password',
			inputType: 'password',
			allowBlank: false,
			listeners: {
				specialkey: function(field, e) {
					if (e.getKey() == e.ENTER) {
						submitForm();
					}
				}
			}
		}, {
			fieldLabel: i18n.login_rememberme,
			name: 'rememberMe',
			xtype: 'checkbox'
		} ],

		buttons: [ {
			text: i18n.login_withuser,
			handler: function() {
				var form = this.up('form').getForm();
				form.setValues({
					username: 'user',
					password: 'user'
				});
				form.submit();
			}
		}, {
			text: i18n.login_withadmin,
			handler: function() {
				var form = this.up('form').getForm();
				form.setValues({
					username: 'admin',
					password: 'admin'
				});
				form.submit();
			}
		}, {
			text: i18n.login,
			handler: function() {
				submitForm();
			}
		} ]
	});

	Ext.create('Ext.container.Viewport', {
		layout: 'border',
		renderTo: Ext.getBody(),

		items: [ {
			region: 'north',
			html: 'e4ds-template',
			cls: 'appHeader',
			height: 35,
			margins: {
				top: 6,
				right: 0,
				bottom: 0,
				left: 6
			}
		}, {
			xtype: 'container',
			region: 'center',
			style: 'background-color: white',
			layout: {
				type: 'vbox',
				align: 'center',
				pack: 'center'
			},
			items: login
		} ]
	});

	function submitForm() {
		var form = login.getForm();
		if (form.isValid()) {
			form.submit();
		}
	}

	// login.show();
	login.getForm().findField('username').focus();

});