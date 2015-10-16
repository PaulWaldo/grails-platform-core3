/* Copyright 2011-2013 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (smaldini@vmware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugin.platform.test

/**
 * @file
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @date 02/01/12

 * @section DESCRIPTION
 *
 * [Does stuff]
 */
class SampleController {

    def sampleService
    def grailsEventsDispatcher

    static navigationScope = "sample"

    def testSave = {
        def user1 =  new Author(name:'Marc').save()
        def user2 =  new Author(name:'Stephane').save()
        render new Book(title: 'test', authors: [user1,user2]).save()
    }

    def testLoad = {

        def book = Book.get(1)
        println 'loading author'
        render book?.authors
    }

    def testRemoveListeners = {
        render removeListeners(params.listener)
    }

    def testInlineListener = {
        def listener = on("afterInsert") {Book book ->
            println "test $book"
        }
        render "$listener registered"
    }

    def index = {
        response.outputStream << "There are ${countListeners('sampleHello')} listeners for topic 'sampleHello' \n"
        response.outputStream << "There are ${countListeners("lal://sampleHello:$SampleService.name")} listeners for class '$SampleService.name' \n"

        response.outputStream << "sync event with replies values : " + event('sampleHello', '{"message":"world"}', [namespace:'lal']).waitFor() + " \n\n"

        event ('platformCore://sampleHello')
        println 'test'

        def async1
        try{
             async1 = event for:'platformCore', topic:'sampleHello',  data:'{"message":"world A"}' , fork:false
        }catch(e){
            log.error e
            log.error e,e
        }
        def async2 = event for:'lal', topic:'sampleHello', data:'{"message":"world B"}'

//        def _stream = stream 'someNamespace://samplehello' | reply { println it } | error { println it } << 'test'
//        _stream.send()

        response.outputStream << "async events replies $async1 $async2 \n\n"
        response.outputStream << "async event reply value " + event('sampleHello', '{"message":"world2"}', [namespace: 'lal']).value + " \n\n"

        response.outputStream << "async wait \n\n"
        def values = waitFor(async1, async2)
        response.outputStream << "waited results : $values \n"
        response.outputStream << "size async1 : ${async1.values} \n"
        response.outputStream << "size async2 : ${async2.size()} \n\n"
        response.outputStream << "async event with on complete\n"

        def reply = { r ->
            println 'hidden test'
        }

        def error = { r ->
            println 'test----' + r
        }

        def r = event topic: 'sampleHella', data: "world 4", for:'lal', onReply:reply, onError:error
        r.cancel()
    }
}
