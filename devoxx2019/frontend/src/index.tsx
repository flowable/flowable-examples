import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import { StationsMapWithApi } from './StationsMapWithApi';
import { DashBoard } from './DashBoard';

import * as serviceWorker from './serviceWorker';
import { HashRouter, Switch, Route } from 'react-router-dom';

ReactDOM.render(
  <HashRouter>
    <Switch>
      <Route exact path="/" component={StationsMapWithApi} />
      <Route path="/dashboard" component={DashBoard} />
    </Switch>
  </HashRouter>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
