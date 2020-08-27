# Add a TestMarker to each item's item metadata
# "foo 0" for the first, "foo 1" for the second, etc.

from net.shibboleth.metadata import TestMarker;

count = 0
for item in items:
    marker = TestMarker('foo {}'.format(count))
    item.getItemMetadata().put(marker)
    count += 1
