import { Link, Route, Routes } from "react-router-dom";
import About from "../pages/About";
import Skills from "../pages/Skills";
import Projects from "../pages/Projects";
import Blog from "../pages/Blog";
import BlogPost from "../pages/BlogPost";

import styles from './App.module.css';


function App() {
  return (
    <>
      <header className={styles.header}>
        <h2>Kim's Portfolio</h2>

        <nav>
          <ul className={styles.menus}>
            <li><Link to='/'>About</Link></li>
            <li><Link to='/skills'>Skills</Link></li>
            <li><Link to='/projects'>Projects</Link></li>
            <li><Link to='/blog'>Blog</Link></li>
          </ul>
        </nav>
      </header>

      <Routes>
        <Route path='/' element={<About />} />
        <Route path='/skills' element={<Skills />} />
        <Route path='/projects' element={<Projects />} />
        <Route path='/blog' element={<Blog />} />
        <Route path='/blog/:slug' element={<BlogPost />} />
      </Routes>
    </>
  )
}

export default App;