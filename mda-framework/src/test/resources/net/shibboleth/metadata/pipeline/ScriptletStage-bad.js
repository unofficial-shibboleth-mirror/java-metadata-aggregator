// Deliberately broken script.
// Add a TestMarker to each item's item metadata
// "foo 0" for the first, "foo 1" for the second, etc.
// BUT throw an exception after the first one.

var TestMarker = Java.type('net.shibboleth.metadata.testing.TestMarker')
for (i=0; i<items.length; i++) {
    item = items[i]
    marker = new TestMarker('foo ' + i);
    item.getItemMetadata().put(marker);
    
    throw 'this is an exception'
}
