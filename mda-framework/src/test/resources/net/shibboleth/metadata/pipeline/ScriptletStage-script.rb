# Add a TestMarker to each item's item metadata
# "foo 0" for the first, "foo 1" for the second, etc.
require 'java'

count = 0

$items.each do |item|
    marker = Java::NetShibbolethMetadataTesting::TestMarker.new("foo #{count}")
    count += 1
    item.itemMetadata.put marker
end
