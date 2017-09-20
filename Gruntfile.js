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
                    }
                ]
            }
        },
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
                    }
                ]
            }
        },
        jshint: {
            options: {
                curly: true,
                eqeqeq: true,
                immed: true,
                latedef: true,
                newcap: true,
                noarg: true,
                sub: true,
                undef: true,
                unused: true,
                boss: true,
                eqnull: true,
                browser: true,
                globals: {
                    jQuery: true
                },
                esnext: true,
                sourceMap: true,
                reporterOutput: ""
            },
            files: ['Gruntfile.js', 'src/main/resources/static/js/*!(.min)!(.babel).js']
        },
        watch: {
            gruntfile: {
                files: 'Gruntfile.js',
                tasks: ['jshint:gruntfile']
            },
            dashoboards: {
                files: ['src/main/resources/static/js/*.js',
                    '!src/main/resources/static/js/*.babel.js',
                    '!src/main/resources/static/js/*.min.js'],
                tasks: ['babel:dashoboards', 'uglify:dashoboards']
            }
        }
    });

    // These plugins provide necessary tasks.
    grunt.loadNpmTasks('grunt-babel');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-watch');

    // Default task.
    grunt.registerTask('default', ['babel', 'uglify']);
    grunt.registerTask('build', ['babel', 'uglify']);
    grunt.registerTask('libs', ['babel:libs', 'uglify:libs'])

};
