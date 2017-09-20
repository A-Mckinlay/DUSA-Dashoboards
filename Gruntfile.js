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
            dist: {
                files: [
                    {
                        expand: true,
                        cwd: 'src/main/resources/static/js/',
                        src: ['*!(.min)!(.babel).js'],
                        dest: 'src/main/resources/static/js/babel',
                        ext: '.babel.js'
                    }
                ]
            }
        },
        uglify: {
            options: {
                banner: '<%= banner %>'
            },
            dist: {
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
                tasks: ['babel', 'uglify']
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

};
