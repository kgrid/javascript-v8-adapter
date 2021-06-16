// k6 run -e URL=http://localhost:8080/node/executive/1.0/process k6post.js -u 10 -i 1000
import http from 'k6/http'
import { Counter, Rate } from 'k6/metrics'

let counter400 = new Counter('Number of 400')
let counter200 = new Counter('Number of 200')
let okRate = new Rate('Percentage ok')

export default function () {
  var url = __ENV.URL
  var payload = JSON.stringify({
    name: 'Steve', spaces: 10,
  })
  var params = {headers: {'Content-Type': 'application/json'}}

  let res = http.post(url, payload, params)
  if (res.status === 400) {
    counter400.add(1)
    okRate.add(0)

  } else if (res.status === 200) {
    counter200.add(1)
    okRate.add(1)
  }

}
// export let options = {
//   scenarios: {
//
//     warmup: {
//       executor: 'constant-vus',
//       // executor-specific configuration
//       tags: { example_tag: 'warmup' },
//       vus: 10,
//       duration: '5s',
//     },
//     forreal: {
//       executor: 'constant-vus',
//       startTime: '5s',
//       // executor-specific configuration
//       tags: { example_tag: 'forreal' },
//       vus: 10,
//       duration: '5s',
//     }
//   }
// }
// export let options = {
//   stages: [
//     {duration: '5s', target: 10}, // simulate ramp-up of traffic from 1 to 60 users over 5 minutes.
//     {duration: '5s', target: 10}, // stay at 60 users for 10 minutes
    // { duration: '3m', target: 100 }, // ramp-up to 100 users over 3 minutes (peak hour starts)
    // { duration: '2m', target: 100 }, // stay at 100 users for short amount of time (peak hour)
    // { duration: '3m', target: 60 }, // ramp-down to 60 users over 3 minutes (peak hour ends)
    // { duration: '10m', target: 60 }, // continue at 60 for additional 10 minutes
    // { duration: '5m', target: 0 }, // ramp-down to 0 users
  // ],
  // thresholds: {
  //   http_req_duration: ['p(99)<1500'], // 99% of requests must complete below 1.5s
  // },
// }
