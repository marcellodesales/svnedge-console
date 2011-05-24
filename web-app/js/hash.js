/* http://rick.measham.id.au/javascript/hash.htm
 * 
 * Copyright © Rick Measham, 2006. All rights reserved.
 * 
 * This library is free software; you can redistribute 
 * it and/or modify it under the GPL or under 
 * the same terms as Perl at your discresion.
 * 
 * @author May 23, 2011: function containsKey(k) added 
 * Marcello de Sales (marcello.desales@gmail.com)
 */
function Hash() {
    for ( var i = 0; i < arguments.length; i++)
        for (n in arguments[i])
            if (arguments[i].hasOwnProperty(n))
                this[n] = arguments[i][n];
}
Hash.prototype = new Object();
Hash.version = 1.04;

Hash.prototype.keys = function() {
    var rv = [];
    for ( var n in this)
        if (this.hasOwnProperty(n))
            rv.push(n);
    return rv;
}

Hash.prototype.length = function() {
    return this.keys().length();
}

Hash.prototype.values = function() {
    var rv = [];
    for ( var n in this)
        if (this.hasOwnProperty(n))
            rv.push(this[n]);
    return rv;
}

Hash.prototype.slice = function() {
    var rv = [];
    for ( var i = 0; i < arguments.length; i++)
        rv.push((this.hasOwnProperty(arguments[i])) ? this[arguments[i]]
                : undefined);
    return rv;
}

Hash.prototype.concat = function() {
    for ( var i = 0; i < arguments.length; i++)
        for ( var n in arguments[i])
            if (arguments[i].hasOwnProperty(n))
                this[n] = arguments[i][n];
    return this;
}

/**
 * Verifies if the Hash contains a given key object.
 */
Hash.prototype.containsKey = function() {
    if (arguments[0] == null) {
        return false
    }
    for ( var n in this)
        if (this.hasOwnProperty(n)) {
            if (n == arguments[0])
                return true
        }
    return false
}

Hash.prototype.remove = function() {
    if (arguments[0] == null) {
        return false
    }
    for ( var n in this)
        if (this.hasOwnProperty(n)) {
            if (n == arguments[0])
                this[n] = null
        }
    return false
}