/*
 * Copyright (c) Microsoft Corporation.  All rights reserved.
 * Licensed under the MIT license. See License.txt in the project root for license information.
 */

exports.defineAutoTests = function () {
  
    describe('Capptain Object', function () {

        it("Capptain should exist", function () {
            expect(Capptain).toBeDefined();
        });

        it("Capptain should contain a startActivity function", function () {
            expect(Capptain.startActivity).toBeDefined();
            expect(typeof Capptain.startActivity == 'function').toBe(true);
        });

        it("Capptain should contain a endActivity function", function () {
            expect(Capptain.endActivity).toBeDefined();
            expect(typeof Capptain.endActivity == 'function').toBe(true);
        });

         it("Capptain should contain a sendAppInfo function", function () {
            expect(Capptain.sendAppInfo).toBeDefined();
            expect(typeof Capptain.sendAppInfo == 'function').toBe(true);
        });     

        it("Capptain should contain a startJob function", function () {
            expect(Capptain.startJob).toBeDefined();
            expect(typeof Capptain.startJob == 'function').toBe(true);
        });

        it("Capptain should contain a endJob function", function () {
            expect(Capptain.endJob).toBeDefined();
            expect(typeof Capptain.endJob == 'function').toBe(true);
        });     

        it("Capptain should contain a onOpenURL function", function () {
            expect(Capptain.onOpenURL).toBeDefined();
            expect(typeof Capptain.onOpenURL == 'function').toBe(true);
        }); 

        it("Capptain should contain a handleOpenURL function", function () {
            expect(Capptain.handleOpenURL).toBeDefined();
            expect(typeof Capptain.handleOpenURL == 'function').toBe(true);
        });     

        it("Capptain should contain a getStatus function", function () {
            expect(Capptain.getStatus).toBeDefined();
            expect(typeof Capptain.getStatus == 'function').toBe(true);
        });     
    });

    describe('Plugin Methods', function() {
        
        var info;
        beforeEach(function(done) {
               Capptain.getStatus(function(_info) {
                    info = _info;
                    done();
                });
        });

        it("Capptain plugin version should be the same as the js plugin version", function () {
                expect(info.pluginVersion).toBeDefined();
                expect(info.pluginVersion == Capptain.pluginVersion).toBe(true);
        });
    });

 };