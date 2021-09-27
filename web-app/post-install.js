// post-install.js

/**
 * Script to run after npm install
 */

'use strict'

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');


const VENDOR_FILES = [
    path.join(__dirname, '/node_modules/antd/dist/antd.css'),
]

const CSS_DEST = path.join(__dirname, '/resources/public/css/vendor/');

function post_install() {
    // create www/css/vendor if not exists
    execSync('mkdir -p resources/public/css/vendor', {cwd: '.', stdio: 'inherit'});

    // finally create www/vendor if not exists and copy antd.css to www/vendor
    if (!fs.existsSync(path.join(__dirname, 'resources/public/css/vendor'))) {
	fs.mkdirSync(path.join(__dirname, '/resources/public/css/vendor'));
    }

    try {
	VENDOR_FILES.forEach(fpath => {
	    fs.copyFileSync(fpath, CSS_DEST + path.basename(fpath))
	});
	console.log('CSS vendor files was copied to ' + CSS_DEST);
    } catch(err) {
	throw err
    }
}

post_install();

