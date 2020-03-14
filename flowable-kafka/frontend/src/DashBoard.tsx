import React, { useState, useEffect } from 'react';
import { ThemeProvider } from 'styled-components';
import { getDashBoardData } from './api';
import { LineChart } from './LineChart';
import { GlobalStyles } from './GlobalStyle';
import { Switch } from './Switch';

import './dashBoard.scss';

const lightTheme = {
  bodyBackground: '#fff',
  bodyColor: '#000'
};

const darkTheme = {
  bodyBackground: '#152642',
  bodyColor: '#777'
};

type DashBoardSerie = {
  [key: string]: number[][];
};

type DashBoardRes = {
  [key: string]: number;
};

type DashBoardProps = {};

const titles: {
  [key: string]: string;
} = {
  caseInstanceCount: 'Case Instance Count',
  processInstanceCount: 'Process Instance Count',
  taskCount: 'Tasks Count',
  reviewEventCount: 'Review Event Count'
};
export const DashBoard = (props: DashBoardProps) => {
  let [data, setFormData] = useState<DashBoardSerie>({
    caseInstanceCount: [],
    processInstanceCount: [],
    taskCount: [],
    reviewEventCount: []
  });
  let [count, setCount] = useState<number>(0);

  const [theme, setTheme] = useState('dark');
  const toggleTheme = () => {
    if (theme === 'light') {
      setTheme('dark');
    } else {
      setTheme('light');
    }
  };

  useEffect(() => {
    let timeout = 2000;

    var timerID = setInterval(() => {
      setCount(count + 1);
    }, timeout);

    return function cleanup() {
      clearInterval(timerID);
    };
  });

  useEffect(() => {
    getDashBoardData().then((res: DashBoardRes) => {
      let newData = data;
      const date = new Date().getTime();
      Object.keys(data).forEach(v => newData[v].push([date, res[v]]));
      setFormData(newData);
    });
  }, [count]);

  return (
    <ThemeProvider theme={theme === 'light' ? lightTheme : darkTheme}>
      <>
        <GlobalStyles />
        <Switch isOn={theme === 'light'} handleToggle={toggleTheme} />
        <div className="flw-dashBoard">
          {Object.keys(data).map(v => (
            <LineChart data={data[v]} name={v} title={titles[v]} theme={theme} />
          ))}
        </div>
      </>
    </ThemeProvider>
  );
};
