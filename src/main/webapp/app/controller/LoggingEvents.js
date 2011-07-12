Ext.define('E4ds.controller.LoggingEvents', {
	extend : 'Ext.app.Controller',

	views : [ 'loggingevent.List' ],
	stores : [ 'LoggingEvents' ],
	models : [ 'LoggingEvent' ],

	refs : [ {
		ref : 'loggingeventList',
		selector : 'loggingeventlist'
	} ],

	init : function() {
		this.control({
			'loggingeventlist' : {
				beforeactivate : this.onBeforeActivate,
			}
		});
	},

	onBeforeActivate : function(cmp, options) {
		console.log('onBeforeActivate');
		if (options) {
			this.doGridRefresh();
		}
	},

	doGridRefresh : function() {
		this.getLoggingeventList().down('pagingtoolbar').doRefresh();
	}

});