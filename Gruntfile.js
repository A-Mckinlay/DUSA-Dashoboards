/*global module:false*/
module.exports = function (grunt) {

    // Project configuration.
    grunt.initConfig({
        // Metadata.
        pkg: grunt.file.readJSON('package.json'),
        banner: '/* Dashoboards - File built <%= grunt.template.today("dd/mm/yyyy") %> */\n',
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
                compact: true,
                sourceMap: true,
                presets: ['env']
            },
            dashoboards: {
                files: [
                    {
                        expand: true,
                        cwd: 'src/main/resources/static/js/',
                        src: ['*!(.min)!(.babel).js'],
                        dest: 'src/main/resources/static/js/',
                        ext: '.min.js'
                    },
                    {
                        expand: true,
                        cwd: 'src/main/resources/static/js/dashoboard-lib',
                        src: ['*!(.min)!(.babel).js'],
                        dest: 'src/main/resources/static/js/lib',
                        ext: '.js'
                    }
                ]
            },
            libs: {
                files: [
                    {
                        src: 'node_modules/moment/moment.js',
                        dest: 'src/main/resources/static/js/lib/moment.js'
                    },
                    {
                        src: 'node_modules/chart.js/dist/Chart.js',
                        dest: 'src/main/resources/static/js/lib/Chart.js'
                    },
                    {
                        src: 'node_modules/d3/build/d3.js',
                        dest: 'src/main/resources/static/js/lib/d3.js'
                    },
                    {
                        src: 'node_modules/c3/c3.js',
                        dest: 'src/main/resources/static/js/lib/c3.js'
                    },
                    {
                        src: 'node_modules/lodash/lodash.js',
                        dest: 'src/main/resources/static/js/lib/lodash.js'
                    },
                    {
                        src: 'node_modules/chroma-js/chroma.js',
                        dest: 'src/main/resources/static/js/lib/chroma.js'
                    },
                    {
                        src: 'node_modules/distinct-colors/dist/distinct-colors.js',
                        dest: 'src/main/resources/static/js/lib/distinct-colors.js'
                    },
                    {
                        src: 'node_modules/mustache/mustache.js',
                        dest: 'src/main/resources/static/js/lib/mustache.js'
                    }
                ]
            }
        },
        watch: {
            dashoboards: {
                files: ['src/main/resources/static/js/*.js',
                    'src/main/resources/static/js/dashoboard-lib/*.js',
                    'src/main/resources/static/js/*.ts',
                    '!src/main/resources/static/js/*.babel.js',
                    '!src/main/resources/static/js/*.min.js'],
                tasks: ['typescript', 'babel:dashoboards']
            }
        }
    });

    // These plugins provide necessary tasks.
    grunt.loadNpmTasks('grunt-babel');
    grunt.loadNpmTasks('grunt-typescript');
    grunt.loadNpmTasks('grunt-contrib-watch');

    // Task definitions.
    grunt.registerTask('default', ['typescript', 'babel']);
    grunt.registerTask('build', ['typescript', 'babel']);
    grunt.registerTask('libs', ['babel:libs']);
    grunt.registerTask('dashoboards', ['babel:dashoboards']);

};
