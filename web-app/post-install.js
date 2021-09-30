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
const SCSS_FILES = [
    path.join(__dirname, '/node_modules/include-media/dist/_include-media.scss'),
]

const CSS_DEST = path.join(__dirname, '/resources/public/css/vendor/');
const SCSS_DEST = path.join(__dirname, '/resources/vendor/');

function post_install() {
    // create vendor dirs if they do not exist
    execSync('mkdir -p resources/public/css/vendor', {cwd: '.', stdio: 'inherit'});
    execSync('mkdir -p resources/vendor', {cwd: '.', stdio: 'inherit'});

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
	      throw err;
    }
    try {
        SCSS_FILES.forEach(fpath => {
            fs.copyFileSync(fpath, SCSS_DEST + path.basename(fpath))
        });
        console.log('SCSS vendor files was copied to ' + SCSS_DEST);
    } catch(err) {
        throw err;
    }
}

post_install();

