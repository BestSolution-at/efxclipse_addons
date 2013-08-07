var parseString = function(value) {
	var parser = new(less.Parser);
	var rv;
	parser.parse(value, function (err, tree) {
		if (!err) {
			rv = tree.toCSS();	
		}
	});
	return rv;
}