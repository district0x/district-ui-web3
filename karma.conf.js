module.exports = function (config) {
    console.log(">>> evaluating karma.conf.js");
    config.set({
        browsers: ['ChromeHeadless'],
        // The directory where the output file lives
        basePath: 'tests-output',
        // The file itself
        files: [{pattern: '/**', included: true, served: true}],
        frameworks: ['cljs-test'],
        plugins: ['karma-cljs-test', 'karma-chrome-launcher'],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
            args: ["doo.runner.doo-tests"],
            singleRun: true
        }
    })
};
