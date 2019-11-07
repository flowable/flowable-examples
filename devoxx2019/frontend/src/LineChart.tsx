import React, { useEffect } from 'react';
import ReactApexChart from 'react-apexcharts';
import ApexCharts from 'apexcharts';
import moment from 'moment';

import './lineChart.scss';

const global = {
  chart: {
    foreColor: '#fff',
    toolbar: {
      show: false
    },
    height: 350,
    type: 'line',
    stacked: true,
    animations: {
      enabled: true,
      easing: 'linear',
      dynamicAnimation: {
        speed: 1000
      }
    },
    dropShadow: {
      enabled: true,
      opacity: 0.3,
      blur: 5,
      left: -7,
      top: 22
    },
    zoom: {
      enabled: false
    }
  },
  colors: ['#FCCF31', '#17ead9', '#f02fc2'],
  stroke: {
    curve: 'straight',
    width: 5
  },
  dataLabels: {
    enabled: false
  },
  grid: {
    borderColor: '#152642',
    padding: {
      left: 0,
      right: 0
    }
  },
  xaxis: {
    type: 'datetime',
    axisTicks: {
      color: '#081B33'
    },
    axisBorder: {
      color: '#081B33'
    }
  },
  fill: {
    type: 'gradient',
    gradient: {
      gradientToColors: ['#F55555', '#6078ea', '#6094ea']
    }
  },
  tooltip: {
    x: {
      formatter: function(val: any) {
        return moment(new Date(val)).format('HH:mm:ss');
      }
    }
  },
  yaxis: {
    decimalsInFloat: 2,
    opposite: true,
    labels: {
      offsetX: -10
    }
  },
  markers: {
    size: 0,
    hover: {
      size: 0
    }
  },
  title: {
    align: 'left',
    style: {
      fontSize: '20px'
    }
  },
  subtitle: {
    text: '20',
    floating: true,
    align: 'right',
    offsetY: 0,
    style: {
      fontSize: '22px'
    }
  },
  legend: {
    show: true,
    floating: true,
    horizontalAlign: 'left',
    onItemClick: {
      toggleDataSeries: false
    },
    position: 'top',
    offsetY: -33,
    offsetX: 60
  }
};

type LineChartProps = {
  data: number[][];
  name: string;
  title?: string;
  theme?: string;
};

export const LineChart = (props: LineChartProps) => {
  useEffect(() => {
    ApexCharts.exec(props.name, 'updateOptions', {
      series: [
        {
          data: props.data
        }
      ],
      subtitle: {
        text: `${props.data[props.data.length - 1] && props.data[props.data.length - 1][1]}`
      }
    });
  }, [props.data.length]);

  const optionsLine = {
    ...global,
    ...{
      chart: {
        ...global.chart,
        id: props.name
      },
      series: [
        {
          name: props.name,
          data: props.data
        }
      ],
      title: {
        ...global.title,
        text: props.title || props.name
      },
      tooltip: {
        ...global.tooltip,
        theme: props.theme
      }
    }
  };
  return (
    <>
      <div className={`flw-lineChart ${props.theme || 'dark'}`}>
        {
          <ReactApexChart
            options={optionsLine}
            series={[
              {
                data: props.data
              }
            ]}
            type="line"
            height="350"
          />
        }
      </div>
    </>
  );
};
