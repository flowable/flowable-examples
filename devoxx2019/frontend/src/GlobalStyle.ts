import { createGlobalStyle } from 'styled-components';

export const GlobalStyles = createGlobalStyle`
  body {
    background: ${({ theme }: any) => theme.bodyBackground};
    color: ${({ theme }: any) => theme.bodyColor};
    transition: all 0.25s linear;
  }
  .body-bg {
    background: #F3F4FA !important;
  }
  
  h1, h2, h3, h4, h5, h6, strong {
    font-weight: 600;
  }
`;
