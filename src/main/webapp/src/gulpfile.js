/**
 * Created by pengshuo on 17/1/7.
 */
var gulp = require('gulp');
var del = require('del');

gulp.task('clean', function (cb) {
    del([
        '../dist'
    ], {force:true});
});

gulp.task('default', ['clean']);