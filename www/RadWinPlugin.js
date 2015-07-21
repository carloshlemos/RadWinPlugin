/*
 * RadWinPlugin
 * Implements the javascript access to the cordova plugin for retrieving the device OID snmp. Returns 0 if not running on Android
 * @author Carlos Henrique
 */

/**
 * @return the oid class instance
 */
 var RadWinPlugin = {

 	getOID: function(params, successCallback, failureCallback){
 		cordova.exec(successCallback, failureCallback, 'RadWinPlugin',
 			'getOID', [params]);
 	}
 };

 module.exports = RadWinPlugin;
