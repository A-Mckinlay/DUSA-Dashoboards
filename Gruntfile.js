/*global module:false*/
module.exports = function (grunt) {

    // Project configuration.
    grunt.initConfig({
        // Metadata.
        pkg: grunt.file.readJSON('package.json'),
        banner: '/*! Dashoboards - ' +
        '<%= grunt.template.today("yyyy-mm-dd") %>\n' +
        '<%= pkg.homepage ? "* " + pkg.homepage + "\\n" : "" %> */\n',
        // Task configuration.
        // Typescript: Javascript with less explosions.
        typescript: {
            base: {
                src: ['src/main/resources/static/js/*.ts'],
                dest: 'src/main/resources/static/js/',
                options: {
                    module: 'amd',
                    target: 'es5',
                    rootDir: 'src/main/resources/static/js/',
                    sourceMap: true,
                    declaration: true
                }
            }
        },
        // I'm still going to keep calling this Babble... -- RT
        babel: {
            options: {
                sourceMap: true,
                presets: ['env']
            },
            dashoboards: {
                files: [
                    {
                        expand: true,
                        cwd: 'src/main/resources/static/js/',
                        src: ['*!(.min)!(.babel).js'],
                        dest: 'src/main/resources/static/js/babel',
                        ext: '.babel.js'
                    },
                ]
            },
            libs: {
                files: [
                    {
                        src: 'node_modules/moment/moment.js',
                        dest: 'src/main/resources/static/js/babel/lib/moment.babel.js'
                    },
                    {
                        src: 'node_modules/chart.js/dist/Chart.js',
                        dest: 'src/main/resources/static/js/babel/lib/Chart.babel.js'
                    },
                    {
                        src: 'node_modules/d3/build/d3.js',
                        dest: 'src/main/resources/static/js/babel/lib/d3.babel.js'
                    }
                ]
            }
        },
        // Slash, Burn, Minify!
        uglify: {
            options: {
                banner: '<%= banner %>'
            },
            dashoboards: {
                files: [
                    {
                        expand: true,
                        cwd: 'src/main/resources/static/js/babel',
                        src: ['*.babel.js'],
                        dest: 'src/main/resources/static/js/',
                        ext: '.min.js',
                        extDot: 'first'
                    }
                ]
            },
            libs: {
                files: [
                    {
                        src: 'src/main/resources/static/js/babel/lib/moment.babel.js',
                        dest: 'src/main/resources/static/js/lib/moment.js'
                    },
                    {
                        src: 'src/main/resources/static/js/babel/lib/Chart.babel.js',
                        dest: 'src/main/resources/static/js/lib/Chart.js'
                    },
                    {
                        src: 'src/main/resources/static/js/babel/lib/d3.babel.js',
                        dest: 'src/main/resources/static/js/lib/d3.js'
                    }
                ]
            }
        },
        watch: {
            dashoboards: {
                files: ['src/main/resources/static/js/*.js',
                    'src/main/resources/static/js/*.ts',
                    '!src/main/resources/static/js/*.babel.js',
                    '!src/main/resources/static/js/*.min.js'],
                tasks: ['typescript', 'babel:dashoboards', 'uglify:dashoboards']
            }
        }
    });

    // These plugins provide necessary tasks.
    grunt.loadNpmTasks('grunt-babel');
    grunt.loadNpmTasks('grunt-typescript');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');

    // Task definitions.
    grunt.registerTask('default', ['typescript', 'babel', 'uglify']);
    grunt.registerTask('build', ['typescript', 'babel', 'uglify']);
    grunt.registerTask('libs', ['babel:libs', 'uglify:libs'])

};
