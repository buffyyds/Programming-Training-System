import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = 'AIGC 数据分析可视化平台';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`${currentYear} ${defaultMessage}`}
      links={[
        // {
        //   key: 'codeNav',
        //   title: '编程导航',
        //   href: 'https://yupi.icu',
        //   blankTarget: true,
        // },
        {
          key: 'Ant Design',
          title: 'Ant Design',
          href: 'https://pro.ant.design/zh-CN/',
          blankTarget: true,
        },
        // {
        //   key: 'github',
        //   title: (
        //     <>
        //       <GithubOutlined /> 源码
        //     </>
        //   ),
        //   href: '',
        //   blankTarget: true,
        // },
      ]}
    />
  );
};
export default Footer;
